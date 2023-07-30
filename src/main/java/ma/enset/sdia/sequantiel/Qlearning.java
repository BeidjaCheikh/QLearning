package ma.enset.sdia.sequantiel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Qlearning {
    private final double ALPHA=0.1;
    private final double GAMMA=0.9;
    private final int MAX_EPOCH=2000;
    private final int GRID_SIZE=6;
    private final int ACTION_SIZE=4;
    private int [][]grid;
    private double [][]qTable=new double[GRID_SIZE*GRID_SIZE][ACTION_SIZE];
    private int [][]actions;
    private int stateI;
    private int stateJ;

    public Qlearning() {
        actions=new int[][]{
                {0,-1},//gauche
                {0,1},//droite
                {1,0},//bas
                {-1,0}//haut
        };
        grid=new int[][]{
                {0,0,0,0,0,0},
                {0,0,0,0,-1,0},
                {0,0,0,0,0,0},
                {0,0,-1,0,0,0},
                {0,0,0,0,0,0},
                {0,0,0,0,0,1}
        };
    }

    private void resetState(){
        stateI=0;
        stateJ=0;
    }

    public int chooseAction(double eps){
        Random rn=new Random();
        double bestQ=0;
        int act = 0;
        if(rn.nextDouble()<eps){
            //exploration
            act=rn.nextInt(ACTION_SIZE);
        }
        else{
            //exploitation
            int st=(stateI*GRID_SIZE)+stateJ;
            for (int i=0 ; i< ACTION_SIZE ; i++){
                if (qTable[st][i]>bestQ){
                    bestQ=qTable[st][i];
                    act=i;
                }
            }
        }
        return act;
    }

    private int executeAction(int act){
        stateI= Math.max(0,Math.min(actions[act][0]+stateI,GRID_SIZE-1));
        stateJ=Math.max(0,Math.min(actions[act][1]+stateJ,GRID_SIZE-1));
        return stateI*GRID_SIZE+stateJ;
    }

    private boolean finiched(){
        return grid[stateI][stateJ]==1;
    }

    private void showResult(){
        System.out.println("********qTable********");
        for (double []line:qTable){
            System.out.printf("[");
            for (double qvalue:line){
                System.out.printf(qvalue+", ");
            }
            System.out.println("]");
        }
        System.out.println(" ");
        resetState();
        while (!finiched()){
            int act=chooseAction(0);
            System.out.println("state: "+(stateI*GRID_SIZE+stateJ)+" action: "+act);
            executeAction(act);
        }
        System.out.println("Final state: "+(stateI*GRID_SIZE+stateJ));
    }

    public void runQlearning(){
        int it=0;
        int currentState;
        int nextState;
        int act,act1;
        while (it<MAX_EPOCH){
            resetState();
            List<Integer> path = new ArrayList<>(); // Liste pour stocker le chemin de l'époque courante
            while (!finiched()) {
                currentState=stateI*GRID_SIZE+stateJ;
                act=chooseAction(0.4);
                nextState = executeAction(act);
                act1=chooseAction(0);
                qTable[currentState][act]=qTable[currentState][act]+ALPHA*(grid[stateI][stateJ]+GAMMA*qTable[nextState][act1]-qTable[currentState][act]);

                path.add(currentState); // Ajouter l'état actuel au chemin

                // Afficher les détails de chaque itération
                System.out.println("Epoch: " + it + " Current State: " + currentState + " Action: " + act + " Next State: " + nextState);
            }
            path.add(stateI*GRID_SIZE+stateJ); // Ajouter l'état final au chemin
            System.out.println("Epoch: " + it + " Path: " + path);

            it++;
        }
        showResult();
    }
}
