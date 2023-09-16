package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class Question {
    private final String question;
    private String answer;
    private final Predicate<String> validator;
    private final String onInvalidAnswer;

    public Question(String question, Predicate<String> validator, String onInvalidAnswer){
        this.question = question;
        this.validator = validator;
        this.onInvalidAnswer = onInvalidAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public void ask(Player who){
        who.sendMessage(Utils.chat(question));
    }

    public void answer(Player who, String answer) {
        if (validator != null && !validator.test(answer))
            who.sendMessage(Utils.chat(onInvalidAnswer));
        else this.answer = answer;
    }

    public Predicate<String> getValidator() {
        return validator;
    }

    public String getOnInvalidAnswer() {
        return onInvalidAnswer;
    }
}
