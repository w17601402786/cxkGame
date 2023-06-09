package com.softeen.ikun.model;

import com.softeen.ikun.GamePanel;
import com.softeen.ikun.tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.softeen.ikun.Config.*;

public class Enemy extends Sprite implements Runnable{


    /**
     * 敌人的生命值
     */
    int hp = ENEMY_HP;

    /**
     * 敌人的移动速度
     */
    int speed = ENEMY_SPEED;

    /**
     * 敌人的动作图像
     */
    List<BufferedImage> enemyImages;

    /**
     * 敌人的队列
     */
    CopyOnWriteArrayList<Enemy> enemies;

    /**
     * 敌人血条的宽度
     */
    private final int hpBarWidth = ENEMY_HP_WIDTH;

    /**
     * 敌人血条的高度
     */
    private final int hpBarHeight = ENEMY_HP_HEIGHT;

    /**
     * 敌人血条的X坐标
     */
    private final int hpBarX = ENEMY_HP_X;

    /**
     * 敌人血条的Y坐标
     */
    private final int hpBarY = ENEMY_HP_Y;



    public Enemy(GamePanel gamePanel, CopyOnWriteArrayList<Enemy> enemies) {

        super(gamePanel);
        this.enemies = enemies;

    }

    @Override
    public void init() {

        enemyImages = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            enemyImages.add(Utils.loading("enemy/" + i + ".png"));
        }
        setImages(enemyImages);

        //X是固定从右边出来
        setX(FRAME_WIDTH-100);
        //Y是随机的
        setY(Utils.randNum(20, FRAME_HEIGHT- 20 - getImg().getHeight()));

        setImageWidth(getImg().getWidth());
        setImageHeight(getImg().getHeight());

    }

    @Override
    public void update() {

    }

    @Override
    public void destroy() {
        enemies.remove(this);
    }

    @Override
    public void run() {

        while (!isDeath()){

            try {
                Thread.sleep(1000/60);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (getGamePanel().pause){
                continue;
            }

            setX(getX() - speed);

            onAttacked();

            if (getX() < getWidth()){
                setDeath(true);
            }


        }
        destroy();
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;

        if (hp <= 0){
            kill();
        }

    }


    /**
     * 被攻击后执行的操作
     */
    void onAttacked(){

        Hero hero = getGamePanel().hero;

        if (hero.isCollision(this)) {
            hero.setHp(hero.getHp()-Utils.randNum(20,100));
            setDeath(true);
        }

    }


    public void kill() {
        getGamePanel().killCount++;
        setDeath(true);

        int randomInt = Utils.randNum(0, DROP_PROBABILITY);

        //随机掉落道具
        if(randomInt == 1 || randomInt == 0){

            //随机生成粥和鸡腿
            Prop prop = new Prop(getGamePanel(),getX(),getY(),randomInt);
            getGamePanel().props.add(prop);
        }

    }


    /**
     * 绘制敌人的血条
     * @param g 画笔对象
     */
    private void drawHpBar(Graphics g){
        // 计算血条的长度
        int hpWidth = (int) (hp / (double)ENEMY_HP * hpBarWidth);
        // 绘制血条的背景框
        g.setColor(Color.BLACK);
        g.drawRect(getX() - hpBarWidth/2 + getImageWidth()/2 , getY() + hpBarY, hpBarWidth, hpBarHeight);

        g.setColor(Color.RED);
        g.fillRect(getX() - hpBarWidth/2 + getImageWidth()/2, getY() + hpBarY, hpWidth, hpBarHeight);

    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        // 绘制血条
        drawHpBar(g);
    }






}
