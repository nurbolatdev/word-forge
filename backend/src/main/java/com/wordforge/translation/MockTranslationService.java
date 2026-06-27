package com.wordforge.translation;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockTranslationService implements TranslationService {

    private static final Map<String, String[]> EN_RU = Map.ofEntries(
        Map.entry("house",   new String[]{"дом", "здание"}),
        Map.entry("book",    new String[]{"книга", "книжка"}),
        Map.entry("water",   new String[]{"вода"}),
        Map.entry("cat",     new String[]{"кот", "кошка"}),
        Map.entry("dog",     new String[]{"собака", "пёс"}),
        Map.entry("apple",   new String[]{"яблоко"}),
        Map.entry("bread",   new String[]{"хлеб"}),
        Map.entry("car",     new String[]{"машина", "автомобиль"}),
        Map.entry("food",    new String[]{"еда", "пища"}),
        Map.entry("love",    new String[]{"любовь", "любить"}),
        Map.entry("friend",  new String[]{"друг", "подруга"}),
        Map.entry("money",   new String[]{"деньги"}),
        Map.entry("game",    new String[]{"игра"}),
        Map.entry("sun",     new String[]{"солнце"}),
        Map.entry("moon",    new String[]{"луна"}),
        Map.entry("door",    new String[]{"дверь"}),
        Map.entry("chair",   new String[]{"стул"}),
        Map.entry("table",   new String[]{"стол"}),
        Map.entry("phone",   new String[]{"телефон"}),
        Map.entry("coffee",  new String[]{"кофе"}),
        Map.entry("tea",     new String[]{"чай"}),
        Map.entry("tree",    new String[]{"дерево"}),
        Map.entry("flower",  new String[]{"цветок"}),
        Map.entry("bird",    new String[]{"птица"}),
        Map.entry("fish",    new String[]{"рыба"}),
        Map.entry("fire",    new String[]{"огонь"}),
        Map.entry("sky",     new String[]{"небо"}),
        Map.entry("sea",     new String[]{"море"}),
        Map.entry("river",   new String[]{"река"}),
        Map.entry("mountain",new String[]{"гора"}),
        Map.entry("road",    new String[]{"дорога"}),
        Map.entry("train",   new String[]{"поезд"}),
        Map.entry("plane",   new String[]{"самолёт"}),
        Map.entry("body",    new String[]{"тело"}),
        Map.entry("heart",   new String[]{"сердце"}),
        Map.entry("head",    new String[]{"голова"}),
        Map.entry("eye",     new String[]{"глаз"}),
        Map.entry("mouth",   new String[]{"рот"}),
        Map.entry("hair",    new String[]{"волосы"}),
        Map.entry("name",    new String[]{"имя"}),
        Map.entry("word",    new String[]{"слово"}),
        Map.entry("language",new String[]{"язык"}),
        Map.entry("music",   new String[]{"музыка"}),
        Map.entry("movie",   new String[]{"фильм"}),
        Map.entry("story",   new String[]{"история", "рассказ"}),
        Map.entry("letter",  new String[]{"письмо", "буква"}),
        Map.entry("number",  new String[]{"число", "номер"}),
        Map.entry("color",   new String[]{"цвет"}),
        Map.entry("light",   new String[]{"свет"}),
        Map.entry("dark",    new String[]{"тёмный", "темнота"}),
        Map.entry("run",     new String[]{"бежать", "бегать"}),
        Map.entry("walk",    new String[]{"идти", "ходить"}),
        Map.entry("eat",     new String[]{"есть", "кушать"}),
        Map.entry("drink",   new String[]{"пить"}),
        Map.entry("sleep",   new String[]{"спать"}),
        Map.entry("speak",   new String[]{"говорить"}),
        Map.entry("write",   new String[]{"писать"}),
        Map.entry("read",    new String[]{"читать"}),
        Map.entry("play",    new String[]{"играть"}),
        Map.entry("learn",   new String[]{"учить", "учиться"}),
        Map.entry("help",    new String[]{"помочь", "помогать"}),
        Map.entry("open",    new String[]{"открывать", "открытый"}),
        Map.entry("close",   new String[]{"закрывать", "закрытый"}),
        Map.entry("buy",     new String[]{"покупать"}),
        Map.entry("sell",    new String[]{"продавать"}),
        Map.entry("happy",   new String[]{"счастливый", "радостный"}),
        Map.entry("sad",     new String[]{"грустный", "печальный"}),
        Map.entry("fast",    new String[]{"быстрый"}),
        Map.entry("slow",    new String[]{"медленный"}),
        Map.entry("hot",     new String[]{"горячий"}),
        Map.entry("cold",    new String[]{"холодный"}),
        Map.entry("white",   new String[]{"белый"}),
        Map.entry("black",   new String[]{"чёрный"}),
        Map.entry("red",     new String[]{"красный"}),
        Map.entry("blue",    new String[]{"синий", "голубой"}),
        Map.entry("green",   new String[]{"зелёный"}),
        Map.entry("yellow",  new String[]{"жёлтый"})
    );

    @Override
    public List<TranslationOption> suggest(String lemma, String sourceLanguage, String targetLanguage) {
        if ("EN".equalsIgnoreCase(sourceLanguage) && "RU".equalsIgnoreCase(targetLanguage)) {
            String[] known = EN_RU.get(lemma.toLowerCase());
            if (known != null) {
                return java.util.Arrays.stream(known)
                        .map(t -> new TranslationOption(t, "mock"))
                        .toList();
            }
        }
        // Generic placeholder when word is not in the dictionary
        return List.of(new TranslationOption("(%s)".formatted(lemma), "mock"));
    }
}
