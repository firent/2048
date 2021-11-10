package ru.ivanpozhidaev;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    int score = 0;
    int maxTile = 0;
    //Previous states
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    boolean isSaveNeeded = true;
    //16
    public void autoMove() {
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(this::left));
        priorityQueue.add(getMoveEfficiency(this::right));
        priorityQueue.add(getMoveEfficiency(this::up));
        priorityQueue.add(getMoveEfficiency(this::down));
        assert priorityQueue.peek() != null;
        priorityQueue.peek().getMove().move();
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles.length; j++) {
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
            previousStates.push(tempTiles);
            int tempScore = score;
            previousScores.push(tempScore);
            isSaveNeeded = false;
    }

    public void rollback() {
        if (!(previousStates.empty() || previousScores.empty())) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove() {
        if (!getEmptyTiles().isEmpty())
            return true;
        for (Tile[] gameTile : gameTiles) {
            for (int j = 1; j < gameTiles.length; j++) {
                if (gameTile[j].value == gameTile[j - 1].value)
                    return true;
            }
        }
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 1; j < gameTiles.length; j++) {
                if (gameTiles[j][i].value == gameTiles[j-1][i].value)
                    return true;
            }
        }
        return false;
    }

    public void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }

        addTile();
        addTile();
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTilesList = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty())
                    emptyTilesList.add(gameTiles[i][j]);
            }
        }
        return emptyTilesList;
    }

    private void addTile() {
        if (getEmptyTiles().size() >= 1) {
            getEmptyTiles().get((int)(Math.random()*getEmptyTiles().size())).value = (Math.random() < 0.9 ? 2 : 4);

        }
    }

    private boolean compressTiles(Tile[] tiles) {
        int[] values = new int[tiles.length];
        for (int i = 0; i < tiles.length; i++)
            values[i] = tiles[i].value;
        Arrays.sort(tiles, Comparator.comparing(Tile::isEmpty));
        boolean isChanged = false;
        for (int i = 0; i < tiles.length; i++) {
            if (values[i] != tiles[i].value) {
                isChanged = true;
                break;
            }
        }
        return isChanged;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isChanged = false;

        for (int i = 0; i < tiles.length-1; i++) {
            if (tiles[i].value == tiles[i+1].value && !tiles[i].isEmpty()) {
                isChanged = true;
                tiles[i].value += tiles[i + 1].value;
                tiles[i+1].value = 0;
                score += tiles[i].value;
                compressTiles(tiles);
                if (maxTile < tiles[i].value)
                    maxTile = tiles[i].value;
            }
        }
        return isChanged;
    }

    public void left() {
        if (isSaveNeeded)
            saveState(gameTiles);

        boolean isChanged = false;
        for (Tile[] tiles : gameTiles) {
            if (compressTiles(tiles) | mergeTiles(tiles))
                isChanged = true;
        }

        if (isChanged)
            addTile();

        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);

        reverseTiles();

        left();

        reverseTiles();
    }

    public void up() {
        saveState(gameTiles);

        rotateTiles();

        left();

        rotateTiles();
        rotateTiles();
        rotateTiles();
    }

    public void down() {
        saveState(gameTiles);

        rotateTiles();
        reverseTiles();

        left();

        reverseTiles();
        rotateTiles();
        rotateTiles();
        rotateTiles();
    }

    private void reverseTiles() {
        Tile[][] tempGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = FIELD_WIDTH-1; i >= 0; i--) {
            for(int j = FIELD_WIDTH-1; j >= 0; j--) {
                tempGameTiles[FIELD_WIDTH-1-i][FIELD_WIDTH-1-j] = gameTiles[i][j];
            }
        }
        gameTiles = tempGameTiles;
    }

    private void rotateTiles() {
        for (int x = 0; x < FIELD_WIDTH/2; x++) {
            for (int y = x; y < FIELD_WIDTH - x - 1; y++) {
                Tile tempTile = gameTiles[x][y];

                // move values from right to top
                gameTiles[x][y] = gameTiles[y][FIELD_WIDTH-1-x];

                // move values from bottom to right
                gameTiles[y][FIELD_WIDTH-1-x] = gameTiles[FIELD_WIDTH-1-x][FIELD_WIDTH-1-y];

                // move values from left to bottom
                gameTiles[FIELD_WIDTH-1-x][FIELD_WIDTH-1-y] = gameTiles[FIELD_WIDTH-1-y][x];

                // assign temp to left
                gameTiles[FIELD_WIDTH-1-y][x] = tempTile;
            }
        }
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        int tilesGame = 0;
        int tilesStack = 0;
        Tile[][] tempTiles = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tilesGame += gameTiles[i][j].value;
                tilesStack += tempTiles[i][j].value;
            }
        }
        return tilesGame != tilesStack;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        int numberOfEmptyTiles = getEmptyTiles().size();
        if (!hasBoardChanged())
            numberOfEmptyTiles = -1;
        rollback();
        return new MoveEfficiency(numberOfEmptyTiles, score, move);
    }
}
