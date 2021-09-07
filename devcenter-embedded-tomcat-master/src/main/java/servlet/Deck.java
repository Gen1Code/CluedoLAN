package servlet;
import java.util.ArrayList;
import java.util.Random;

public class Deck {
    ArrayList<Integer> list = new ArrayList();
    Random rnd = new Random();

    public Deck(int cardNo){
        for(int i=0;i<cardNo;i++){
            list.add(i);
        }
    }

    public int giveCard(){
        int CardIndex = rnd.nextInt(list.size());
        int card = list.get(CardIndex);
        list.remove(CardIndex);
        return card;
    }
    // Removes a Card, if card doesn't exist nothing happens
    public void removeCard(int value){
        int index= list.indexOf(value);
        if (index != -1) {
            list.remove(index);
        }
    }
    public int deckSize(){
        return list.size();
    }

}
