package com.example.beebliotheca.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;


public class AnimationFloatAdapter extends View {

    private Paint paint;
    private Random random;
    private int canvasWidth;
    private int canvasHeight;
    private float minSpeed = 0.5f;
    private float maxSpeed = 5f;
    private float minSize = 40f;
    private float maxSize = 150f;
    String[] ancientCharacters = {
            "𓀀", "𓀁", "𓀂", "𓀃", "𓀄", "𓀅", "𓀆", "𓀇", "𓀈", "𓀉",
            "𓀊", "𓀋", "𓀌", "𓀍", "𓀎", "𓀏", "𓀐", "𓀑", "𓀒", "𓀓",
            "𓀔", "𓀕", "𓀖", "𓀗", "𓀘", "𓀙", "𓀚", "𓀛", "𓀜", "𓀝",
            "𓀞", "𓀟", "𓀠", "𓀡", "𓀢", "𓀣", "𓀤", "𓀥", "𓀦", "𓀧",
            "𓀨", "𓀩", "𓀪", "𓀫", "𓀬", "𓀭", "𓀮", "𓀯", "𓀰", "𓀱"
    };

    private class FloatingCharacter {
        float xPos;
        float yPos;
        float size;
        float speed;
        String character;
        int opacity;

        FloatingCharacter(float xPos, float yPos, float size, float speed, String character, int opacity) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.size = size;
            this.speed = speed;
            this.character = character;
            this.opacity = opacity;
        }
    }

    private FloatingCharacter[] floatingCharacters;

    public AnimationFloatAdapter(Context context) {
        super(context);
        init();
    }

    public AnimationFloatAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationFloatAdapter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setFakeBoldText(true);

        random = new Random();

        createFloatingCharacters();
    }

    private void createFloatingCharacters() {
        int numCharacters = ancientCharacters.length;
        floatingCharacters = new FloatingCharacter[numCharacters];

        for (int i = 0; i < numCharacters; i++) {
            float xPos = random.nextFloat() * canvasWidth;
            float yPos = random.nextFloat() * canvasHeight;
            float size = minSize + random.nextFloat() * (maxSize - minSize);
            float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
            String character = ancientCharacters[i];
            int opacity = random.nextInt(155) + 100; // Random opacity between 100 and 255

            floatingCharacters[i] = new FloatingCharacter(xPos, yPos, size, speed, character, opacity);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasWidth = w;
        canvasHeight = h;

        createFloatingCharacters();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.parseColor("#00FFFFFF"));

        for (FloatingCharacter character : floatingCharacters) {
            character.yPos += 1;

            if (character.yPos > canvasHeight) {
                character.yPos = -character.size;
                character.xPos = random.nextFloat() * canvasWidth;
                character.opacity = random.nextInt(155) + 100;
            }

            paint.setTextSize(character.size);
            paint.setAlpha(character.opacity);
            canvas.drawText(character.character, character.xPos, character.yPos, paint);
        }

        invalidate();
    }
}
