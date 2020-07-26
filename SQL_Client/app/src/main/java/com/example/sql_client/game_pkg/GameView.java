package com.example.sql_client.game_pkg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.sql_client.game_pkg.activities_pkg.GameActivity;
import com.example.sql_client.pop_up.PopUpHandler;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View
{
    private enum Direction
    {
        UP, DOWN, LEFT, RIGHT
    };

    private Cell[][] cells;
    volatile private Cell playerCell = null, exitCell = null;

    static private final int COLS = 7, ROWS = 10, TMAX = (int)(3*60)/*sec*/, TMIN =(int)(0.75*60) /*sec*/, LEVEL_MAX = 5;
    private static final float WALL_THICKNESS = 4.0f;

    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random random;

    private GameActivity gameActivity = null;
    private Thread TimeElpaseTH = null;

    public GameView(Context context, @Nullable AttributeSet attrs )
    {
        super(context, attrs);

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.BLUE);

        exitPaint = new Paint();
        exitPaint.setColor(Color.RED);

        random = new Random();

        Player.resetTmpScore();
    }

    private class TimeElapse implements Runnable {
        public void run() {
            while (TMAX != gameActivity.getSecondsFromDurationString());
            final String text ="LEVEL: " + String.valueOf(Player.getTmpLevel()) + "\nPOINTS: " + String.valueOf(Player.getTmpPoints());
            PopUpHandler.PopUp(gameActivity,0,"Time Elapsed..",text, gameActivity.getListener() );
        }
    }

    public void setGameActivity(GameActivity gameActivity)
    {
        this.gameActivity = gameActivity;
    }

    private Cell getNeighbour(Cell cell)
    {
        ArrayList<Cell> neighbours = new ArrayList<>();

        //left neighbour
        if(cell.col > 0)
            if(!cells[cell.col-1][cell.row].visited)
                neighbours.add(cells[cell.col-1][cell.row]);
        //right neighbour
        if(cell.col < COLS-1)
            if(!cells[cell.col+1][cell.row].visited)
                neighbours.add(cells[cell.col+1][cell.row]);

        //top neighbour
        if(cell.row > 0)
            if(!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);

        //bottom neighbour
        if(cell.row < ROWS-1)
            if(!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);

        if(neighbours.size() > 0)
        {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell current, Cell next)
    {
        if(current.col == next.col && current.row == next.row+1)
        {
            current.topWall = false;
            next.bottomWall = false;
        }
        if(current.col == next.col && current.row == next.row-1)
        {
            current.bottomWall = false;
            next.topWall = false;
        }
        if(current.col == next.col+1 && current.row == next.row)
        {
            current.leftWall = false;
            next.rightWall = false;
        }
        if(current.col == next.col-1 && current.row == next.row)
        {
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    public void createMaze(boolean includePlayer)
    {
        if(true == includePlayer && null == TimeElpaseTH)
        {
            TimeElpaseTH = new Thread(new TimeElapse() );
            TimeElpaseTH.start();
        }

        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];

        for(int x=0; x<COLS; x++)
        {
            for(int y=0; y<ROWS; y++)
            {
                cells[x][y] = new Cell(x,y);
            }
        }

        if(true == includePlayer) {
            playerCell = cells[0][0];
        }
        exitCell = cells[COLS-1][ROWS-1];

        current = cells[0][0];
        current.visited = true;

        do {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else
                current = stack.pop();
        }while(!stack.empty() );
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawColor(Color.GREEN);

        final int width = getWidth();
        final int height = getHeight();

        if ((float)(width / height) < (float)(COLS / ROWS) )
            cellSize = width / (COLS + 1);
        else
            cellSize = height / (ROWS + 1);

        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height-ROWS*cellSize)/2;

        canvas.translate(hMargin, vMargin);

        for(int x=0; x<COLS; x++)
        {
            for(int y=0; y<ROWS; y++)
            {
                if(cells[x][y].topWall)
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallPaint
                    );

                if(cells[x][y].leftWall)
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );

                if(cells[x][y].bottomWall)
                    canvas.drawLine(
                            x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );

                if(cells[x][y].rightWall)
                    canvas.drawLine(
                            (x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
            }
        }

        float margin = cellSize/10;
        if(playerCell != null) {
            canvas.drawRect(
                    playerCell.col * cellSize + margin,
                    playerCell.row * cellSize + margin,
                    (playerCell.col + 1) * cellSize - margin,
                    (playerCell.row + 1) * cellSize - margin,
                    playerPaint
            );
        }

        canvas.drawRect(
                exitCell.col*cellSize+margin,
                exitCell.row*cellSize+margin,
                (exitCell.col+1)*cellSize-margin,
                (exitCell.row+1)*cellSize-margin,
                exitPaint
        );
    }

    private void movePlayer(Direction direction)
    {
        if(null == playerCell)
        {
            return;
        }
        switch (direction) {
            case UP:
                if (!playerCell.topWall)
                    playerCell = cells[playerCell.col][playerCell.row - 1];
                break;

            case DOWN:
                if (!playerCell.bottomWall)
                    playerCell = cells[playerCell.col][playerCell.row + 1];
                break;

            case LEFT:
                if (!playerCell.leftWall)
                    playerCell = cells[playerCell.col - 1][playerCell.row];
                break;

            case RIGHT:
                if (!playerCell.rightWall)
                    playerCell = cells[playerCell.col + 1][playerCell.row];
                break;
        }
        checkExit();
        invalidate();

    }

    private void checkExit()
    {
        if(null == playerCell)
        {
            return;
        }
        if (playerCell == exitCell) {
            boolean includePlayer = false;
            final int level = Player.getTmpLevel()+1;
            if(level < LEVEL_MAX) {
                includePlayer = true;
            }
            final float timePerc = (float)(TMAX*level/5-gameActivity.getSecondsFromDurationString() ) / (float)(TMAX*level/5-TMIN*level/5);
            final int points = (int)(timePerc*(float)(5*level/5*100));
            Player.setRecord(level, points);

            int incr=1;
            if(!includePlayer){
                incr=0;
            }

            final String result = "Level: " + String.valueOf(Player.getTmpLevel()+incr) + "\tPoints: " + String.valueOf(Player.getTmpPoints() );
            gameActivity.streamResult(result);
            createMaze(includePlayer);
            if(!includePlayer) {
                final String text ="LEVEL: " + String.valueOf(Player.getTmpLevel()) + "\nPOINTS: " + String.valueOf(Player.getTmpPoints());
                PopUpHandler.PopUp(gameActivity, 0, "Game finished...", text,gameActivity.getListener() );
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(null == playerCell)
        {
            return true;
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN)
            return true;

        if(event.getAction() == MotionEvent.ACTION_MOVE )
        {
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = hMargin + (playerCell.col+0.5f)*cellSize;
            float playerCenterY = vMargin + (playerCell.row+0.5f)*cellSize;

            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);

            if(absDx > cellSize || absDy > cellSize)
            {
                if(absDx > absDy)
                {
                    //move in x direction
                    if(dx > 0)
                        //move to the right
                        movePlayer(Direction.RIGHT);
                    else
                        //move to the left
                        movePlayer(Direction.LEFT);
                }
                else
                {
                    //move in y direction
                    if(dy > 0)
                        //move down
                        movePlayer(Direction.DOWN);
                    else
                        //move up
                        movePlayer(Direction.UP);
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private class Cell{
        boolean
            topWall = true,
            leftWall = true,
            bottomWall = true,
            rightWall = true,
            visited = false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
