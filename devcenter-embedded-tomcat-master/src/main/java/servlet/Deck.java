package servlet;
import java.util.ArrayList;
import java.util.Random;

public class Deck {
    ArrayList<Integer> list = new ArrayList();
    Random rnd = new Random();

    //Creates a deck with x amount of cards
    public Deck(int cardStart,int cardNo){
        for(int i=cardStart;i<cardStart+cardNo;i++){
            list.add(i);
        }
    }
    //Second Constructor that joins lists
    public Deck(ArrayList<Integer> list1,ArrayList<Integer> list2,ArrayList<Integer> list3){
        list.addAll(list1);
        list.addAll(list2);
        list.addAll(list3);
    }
    //removes a card and returns it
    public int giveCard(){
        int CardIndex = rnd.nextInt(list.size());
        int card = list.get(CardIndex);
        list.remove(CardIndex);
        return card;
    }

    public ArrayList<Integer> returnList(){
        return list;
    }

    // Removes a Card, if card doesn't exist, nothing happens
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
