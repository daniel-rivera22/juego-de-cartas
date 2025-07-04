package es.daniylorena.juegodecartas.state;

import java.util.List;

public class Player {

    private final String name;
    private List<Card> hand;
    private Role role;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int numberOfCards() {
        return hand.size();
    }

    public void addCardToHand(Card card) {
        this.hand.add(card);
    }
}
