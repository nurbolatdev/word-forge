package com.wordforge.quiz;

import com.wordforge.enrichment.WordEnrichmentRepository;
import com.wordforge.enrichment.WordExampleRepository;
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
import java.util.regex.Pattern;

@Service
class QuizService {

    private final QuizRoundRepository roundRepo;
    private final ReviewRepository reviewRepo;
    private final UserCardRepository cardRepo;
    private final WordListRepository listRepo;
    private final WordTranslationRepository translationRepo;
    private final WordEnrichmentRepository enrichmentRepo;
    private final WordExampleRepository exampleRepo;
    private final SchedulerService schedulerService;

    QuizService(QuizRoundRepository roundRepo, ReviewRepository reviewRepo,
                UserCardRepository cardRepo, WordListRepository listRepo,
                WordTranslationRepository translationRepo,
                WordEnrichmentRepository enrichmentRepo,
                WordExampleRepository exampleRepo,
                SchedulerService schedulerService) {
        this.roundRepo = roundRepo;
        this.reviewRepo = reviewRepo;
        this.cardRepo = cardRepo;
        this.listRepo = listRepo;
        this.translationRepo = translationRepo;
        this.enrichmentRepo = enrichmentRepo;
        this.exampleRepo = exampleRepo;
        this.schedulerService = schedulerService;
    }

    @Transactional
    QuizRoundDto startRound(Long userId, List<Long> cardIds, String modality, String direction) {
        roundRepo.findFirstByUserIdAndFinishedAtIsNullOrderByStartedAtDesc(userId)
                .ifPresent(r -> {
                    r.setFinishedAt(OffsetDateTime.now());
                    roundRepo.save(r);
                });
        String dir = direction != null ? direction : "EN_RU";
        QuizRound round = roundRepo.save(new QuizRound(userId, cardIds.toArray(Long[]::new), modality, dir));
        return QuizRoundDto.from(round, 0);
    }

    QuizQuestionDto getQuestion(Long roundId, Long userId) {
        QuizRound round = requireRound(roundId, userId);
        Long[] cardIds = round.getCardIds();
        int index = answeredCount(roundId, cardIds);
        if (index >= cardIds.length) {
            throw new ResponseStatusException(HttpStatus.GONE, "Round is complete");
        }
        return buildQuestion(cardIds[index], index, cardIds.length, round.getModality(), round.getDirection(), cardIds);
    }

    @Transactional
    AnswerResultDto submitAnswer(Long roundId, Long userId, Long cardId,
                                 Long chosenTranslationId, String typedAnswer, long responseTimeMs) {
        QuizRound round = requireRound(roundId, userId);
        if (reviewRepo.existsByRoundIdAndCardId(roundId, cardId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already answered in this round");
        }

        UserCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean correct;
        String taskType;
        String modality = round.getModality();
        String direction = round.getDirection();
        boolean isRuEn = "RU_EN".equals(direction);

        if ("CLOZE".equals(modality)) {
            // CLOZE is always EN_RU: type the English lemma
            taskType = "CLOZE";
            String typed = typedAnswer == null ? "" : typedAnswer.strip().toLowerCase();
            correct = card.getLemma().strip().toLowerCase().equals(typed);
        } else if ("TYPING".equals(modality)) {
            taskType = "TYPING";
            String typed = typedAnswer == null ? "" : typedAnswer.strip().toLowerCase();
            if (isRuEn) {
                // RU_EN: show Russian translation, user types English lemma
                correct = card.getLemma().strip().toLowerCase().equals(typed);
            } else {
                // EN_RU: show English lemma, user types Russian translation
                correct = Arrays.stream(card.getChosenTranslationIds())
                        .map(id -> translationRepo.findById(id)
                                .map(t -> t.getText().strip().toLowerCase()).orElse(""))
                        .anyMatch(t -> t.equals(typed));
            }
        } else {
            // MCQ: grading is the same regardless of direction — submitted translationId
            // must be in the correct card's chosenTranslationIds
            taskType = "CHOICE_4";
            correct = Arrays.asList(card.getChosenTranslationIds()).contains(chosenTranslationId);
        }

        var gradeResult = schedulerService.grade(cardId, userId, correct, responseTimeMs);

        Long correctTranslationId = card.getChosenTranslationIds().length > 0
                ? card.getChosenTranslationIds()[0] : chosenTranslationId;

        String correctText;
        if ("CLOZE".equals(modality)) {
            correctText = card.getLemma();
        } else if (isRuEn && "TYPING".equals(modality)) {
            correctText = card.getLemma();
        } else {
            correctText = translationRepo.findById(correctTranslationId)
                    .map(WordTranslation::getText).orElse("");
        }

        reviewRepo.save(new Review(cardId, userId, roundId, taskType, "TEXT",
                (short) gradeResult.grade(), correct, (int) responseTimeMs, false));

        Long[] cardIds = round.getCardIds();
        int answered = answeredCount(roundId, cardIds);
        boolean finished = answered >= cardIds.length;

        if (finished) {
            round.setFinishedAt(OffsetDateTime.now());
            roundRepo.save(round);
        }

        QuizQuestionDto next = finished ? null
                : buildQuestion(cardIds[answered], answered, cardIds.length, modality, direction, cardIds);
        return new AnswerResultDto(cardId, correct, gradeResult.grade(),
                correctTranslationId, correctText, finished, next);
    }

    private QuizQuestionDto buildQuestion(Long cardId, int index, int total,
                                          String modality, String direction, Long[] roundCardIds) {
        UserCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long correctTranslationId = card.getChosenTranslationIds().length > 0
                ? card.getChosenTranslationIds()[0] : null;
        if (correctTranslationId == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Card has no chosen translations");
        }

        boolean isRuEn = "RU_EN".equals(direction);

        // CLOZE is always EN_RU regardless of direction setting
        if ("CLOZE".equals(modality)) {
            String clozeText = buildClozeText(card);
            String prompt = card.getLemma();
            return new QuizQuestionDto(cardId, card.getLemma(), prompt, "EN_RU",
                    index, total, modality, clozeText, List.of());
        }

        WordTranslation correctTranslation = translationRepo.findById(correctTranslationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // promptText: what the user sees as the question
        String promptText = isRuEn ? correctTranslation.getText() : card.getLemma();

        if ("TYPING".equals(modality)) {
            return new QuizQuestionDto(cardId, card.getLemma(), promptText, direction,
                    index, total, modality, null, List.of());
        }

        // MCQ: build options
        List<QuizQuestionDto.OptionDto> options = new ArrayList<>();

        if (isRuEn) {
            // RU_EN MCQ: show Russian translation, options are English lemmas
            // Correct option: this card's lemma, identified by its translationId
            options.add(new QuizQuestionDto.OptionDto(correctTranslationId, card.getLemma()));

            // Distractors: other cards in this round (by their lemma)
            Arrays.stream(roundCardIds)
                    .filter(cid -> !cid.equals(cardId))
                    .limit(3)
                    .forEach(cid -> cardRepo.findById(cid).ifPresent(other -> {
                        Long otherId = other.getChosenTranslationIds().length > 0
                                ? other.getChosenTranslationIds()[0] : null;
                        if (otherId != null) {
                            options.add(new QuizQuestionDto.OptionDto(otherId, other.getLemma()));
                        }
                    }));
        } else {
            // EN_RU MCQ: show English lemma, options are Russian translations
            WordList list = listRepo.findById(card.getListId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            String cefrLevel = enrichmentRepo.findFirstByWordId(card.getWordId())
                    .map(e -> e.getCefrLevel()).orElse("B1");
            List<WordTranslation> pool = translationRepo.findSmartDistractors(
                    list.getTargetLang(), card.getWordId(), cefrLevel);
            options.add(new QuizQuestionDto.OptionDto(correctTranslation.getId(), correctTranslation.getText()));
            pool.stream().limit(3)
                    .forEach(d -> options.add(new QuizQuestionDto.OptionDto(d.getId(), d.getText())));
        }

        // Pad with placeholders if fewer than 4 options (rare edge case)
        while (options.size() < 4) {
            options.add(new QuizQuestionDto.OptionDto(-1L, "—"));
        }

        Collections.shuffle(options);
        return new QuizQuestionDto(cardId, card.getLemma(), promptText, direction,
                index, total, modality, null, options);
    }

    private String buildClozeText(UserCard card) {
        var enrichmentOpt = enrichmentRepo.findFirstByWordId(card.getWordId());
        if (enrichmentOpt.isEmpty()) return "Fill in: ___";

        var examples = exampleRepo.findByEnrichmentId(enrichmentOpt.get().getId());
        if (examples.isEmpty()) return "Fill in: ___";

        String sentence = examples.get(0).getText();
        String pattern = "(?i)\\b" + Pattern.quote(card.getLemma()) + "\\b";
        String blanked = sentence.replaceAll(pattern, "___");
        return blanked.contains("___") ? blanked : sentence + " [___]";
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
