package com.wordforge.quiz;

import com.wordforge.enrichment.WordEnrichmentRepository;
import com.wordforge.lists.UserCard;
import com.wordforge.lists.UserCardRepository;
import com.wordforge.lists.WordList;
import com.wordforge.lists.WordListRepository;
import com.wordforge.scheduler.SchedulerService;
import com.wordforge.vocabulary.WordTranslation;
import com.wordforge.vocabulary.WordTranslationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
class QuizService {

    private final QuizRoundRepository roundRepo;
    private final ReviewRepository reviewRepo;
    private final UserCardRepository cardRepo;
    private final WordListRepository listRepo;
    private final WordTranslationRepository translationRepo;
    private final WordEnrichmentRepository enrichmentRepo;
    private final SchedulerService schedulerService;

    QuizService(QuizRoundRepository roundRepo, ReviewRepository reviewRepo,
                UserCardRepository cardRepo, WordListRepository listRepo,
                WordTranslationRepository translationRepo,
                WordEnrichmentRepository enrichmentRepo,
                SchedulerService schedulerService) {
        this.roundRepo = roundRepo;
        this.reviewRepo = reviewRepo;
        this.cardRepo = cardRepo;
        this.listRepo = listRepo;
        this.translationRepo = translationRepo;
        this.enrichmentRepo = enrichmentRepo;
        this.schedulerService = schedulerService;
    }

    @Transactional
    QuizRoundDto startRound(Long userId, List<Long> cardIds) {
        roundRepo.findFirstByUserIdAndFinishedAtIsNullOrderByStartedAtDesc(userId)
                .ifPresent(r -> {
                    r.setFinishedAt(OffsetDateTime.now());
                    roundRepo.save(r);
                });
        QuizRound round = roundRepo.save(new QuizRound(userId, cardIds.toArray(Long[]::new)));
        return QuizRoundDto.from(round, 0);
    }

    QuizQuestionDto getQuestion(Long roundId, Long userId) {
        QuizRound round = requireRound(roundId, userId);
        Long[] cardIds = round.getCardIds();
        int index = answeredCount(roundId, cardIds);
        if (index >= cardIds.length) {
            throw new ResponseStatusException(HttpStatus.GONE, "Round is complete");
        }
        return buildQuestion(cardIds[index], index, cardIds.length);
    }

    @Transactional
    AnswerResultDto submitAnswer(Long roundId, Long userId, Long cardId,
                                 Long chosenTranslationId, long responseTimeMs) {
        QuizRound round = requireRound(roundId, userId);
        if (reviewRepo.existsByRoundIdAndCardId(roundId, cardId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already answered in this round");
        }

        UserCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean correct = Arrays.asList(card.getChosenTranslationIds()).contains(chosenTranslationId);

        var gradeResult = schedulerService.grade(cardId, userId, correct, responseTimeMs);

        Long correctTranslationId = card.getChosenTranslationIds().length > 0
                ? card.getChosenTranslationIds()[0] : chosenTranslationId;
        String correctText = translationRepo.findById(correctTranslationId)
                .map(WordTranslation::getText).orElse("");

        reviewRepo.save(new Review(cardId, userId, roundId, "CHOICE_4", "TEXT",
                (short) gradeResult.grade(), correct, (int) responseTimeMs, false));

        Long[] cardIds = round.getCardIds();
        int answered = answeredCount(roundId, cardIds);
        boolean finished = answered >= cardIds.length;

        if (finished) {
            round.setFinishedAt(OffsetDateTime.now());
            roundRepo.save(round);
        }

        QuizQuestionDto next = finished ? null : buildQuestion(cardIds[answered], answered, cardIds.length);
        return new AnswerResultDto(cardId, correct, gradeResult.grade(),
                correctTranslationId, correctText, finished, next);
    }

    private QuizQuestionDto buildQuestion(Long cardId, int index, int total) {
        UserCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        WordList list = listRepo.findById(card.getListId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long correctTranslationId = card.getChosenTranslationIds().length > 0
                ? card.getChosenTranslationIds()[0] : null;
        if (correctTranslationId == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Card has no chosen translations");
        }

        WordTranslation correct = translationRepo.findById(correctTranslationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String cefrLevel = enrichmentRepo.findFirstByWordId(card.getWordId())
                .map(e -> e.getCefrLevel())
                .orElse("B1");
        List<WordTranslation> pool = translationRepo.findSmartDistractors(
                list.getTargetLang(), card.getWordId(), cefrLevel);
        List<WordTranslation> distractors = pool.stream().limit(3).toList();

        List<QuizQuestionDto.OptionDto> options = new ArrayList<>();
        options.add(new QuizQuestionDto.OptionDto(correct.getId(), correct.getText()));
        distractors.forEach(d -> options.add(new QuizQuestionDto.OptionDto(d.getId(), d.getText())));
        Collections.shuffle(options);

        return new QuizQuestionDto(cardId, card.getLemma(), index, total, options);
    }

    private QuizRound requireRound(Long roundId, Long userId) {
        QuizRound round = roundRepo.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!round.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return round;
    }

    private int answeredCount(Long roundId, Long[] cardIds) {
        return (int) Arrays.stream(cardIds)
                .filter(cid -> reviewRepo.existsByRoundIdAndCardId(roundId, cid))
                .count();
    }
}
