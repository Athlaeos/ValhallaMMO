package me.athlaeos.valhallammo.dom;

import org.bukkit.entity.Player;

import java.util.*;

public class Questionnaire {
    private static final Map<UUID, Questionnaire> questioningPlayers = new HashMap<>();

    public static void startQuestionnaire(Player who, Questionnaire questionnaire){
        questioningPlayers.put(who.getUniqueId(), questionnaire);
        questionnaire.start();
    }

    public static Questionnaire getActiveQuestionnaire(Player who){
        return questioningPlayers.get(who.getUniqueId());
    }

    private final Player who;
    private final List<Question> questions;
    private final Action<Player> onStart;
    private final Action<Player> onFinish;

    public Questionnaire(Player who, Action<Player> onStart, Action<Player> onFinish, Question... questions){
        this.who = who;
        this.questions = Arrays.asList(questions);
        this.onStart = onStart;
        this.onFinish = onFinish;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public Action<Player> getOnStart() {
        return onStart;
    }

    public Player getWho() {
        return who;
    }

    public Action<Player> getOnFinish() {
        return onFinish;
    }

    public void start(){
        if (onStart != null) getOnStart().act(getWho());
        Question next = nextQuestion();
        if (next == null) finish();
        else next.ask(getWho());
    }

    public Question nextQuestion(){
        if (allAnswered()) {
            finish();
            return null;
        } else return getQuestions().stream().filter(q -> q.getAnswer() == null).findFirst().orElse(null);
    }

    public boolean allAnswered(){
        return getQuestions().stream().allMatch(q -> q.getAnswer() != null);
    }

    public void finish(){
        questioningPlayers.remove(who.getUniqueId());
        if (getOnFinish() != null) getOnFinish().act(who);
    }
}
