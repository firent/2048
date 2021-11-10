package ru.ivanpozhidaev;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controller extends KeyAdapter {
    Model model;
    View view;
    private static final int WINNING_TILE = 2048;

    public Controller(Model model) {
        this.model = model;
        this.view = new View(this);
    }

    public View getView() {
        return view;
    }

    public void resetGame() {
        model.score = 0;
        model.maxTile = 0;
        view.isGameWon = false;
        view.isGameLost = false;
        model.resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return model.getGameTiles();
    }

    public int getScore() {
        return model.score;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            resetGame();
        else if (e.getKeyCode() == KeyEvent.VK_Z)
            model.rollback();
        else if (e.getKeyCode() == KeyEvent.VK_R)
            model.randomMove();
        //16
        else if (e.getKeyCode() == KeyEvent.VK_A)
            model.autoMove();
        else if (!model.canMove())
            view.isGameLost = true;
        else if (!view.isGameWon && !view.isGameLost) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT)
                model.left();
            else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                model.right();
            else if (e.getKeyCode() == KeyEvent.VK_UP)
                model.up();
            else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                model.down();
        }

        if (model.maxTile == WINNING_TILE)
            view.isGameWon = true;

        view.repaint();
    }
}
