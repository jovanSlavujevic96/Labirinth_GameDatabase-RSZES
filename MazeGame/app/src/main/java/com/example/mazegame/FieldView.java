package com.example.mazegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class FieldView extends View
{
    private class Cell
    {
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

    private enum Direction
    {
        UP, DOWN, LEFT, RIGHT
    };

    private FieldView.Cell[][] cells;
    private FieldView.Cell exit;
    private static final int COLS = 7, ROWS = 10;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random random;

    public FieldView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        exitPaint = new Paint();
        exitPaint.setColor(Color.RED);

        random = new Random();

        createMaze();
    }

    private void createMaze()
    {
        Stack<FieldView.Cell> stack = new Stack<>();
        FieldView.Cell current, next;

        cells = new FieldView.Cell[COLS][ROWS];

        for(int x=0; x<COLS; x++)
        {
            for(int y=0; y<ROWS; y++)
            {
                cells[x][y] = new FieldView.Cell(x,y);
            }
        }

        exit = cells[COLS-1][ROWS-1];

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

    private FieldView.Cell getNeighbour(FieldView.Cell cell)
    {
        ArrayList<FieldView.Cell> neighbours = new ArrayList<>();

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

    private void removeWall(FieldView.Cell current, FieldView.Cell next)
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

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GREEN);

        int width = getWidth();
        int height = getHeight();

        if (width / height < COLS / ROWS)
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

        canvas.drawRect(
                exit.col*cellSize+margin,
                exit.row*cellSize+margin,
                (exit.col+1)*cellSize-margin,
                (exit.row+1)*cellSize-margin,
                exitPaint
        );
    }
}
