package com.softeen.ikun.model;

import com.softeen.ikun.Config;
import com.softeen.ikun.GamePanel;
import com.softeen.ikun.MusicPlayer;
import com.softeen.ikun.tools.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.softeen.ikun.Config.*;
import static com.softeen.ikun.MusicPlayer.ATTACK_MUSIC;
import static com.softeen.ikun.MusicPlayer.SKILL_MUSIC;

/**
 * 英雄类
 */
public class Hero extends Sprite {


    /**
     * 英雄的生命值
     */
    int hp = PLAYER_HP;

    /**
     * 英雄的怒气值
     */
    int mp = 0;

    /**
     * 移动方向
     * true:向右
     * false:向左
     */
    boolean direction = true;

    /**
     * 向右移动的图片动画帧
     */
    List<BufferedImage> imagesRight;

    /**
     * 向左移动的图片动画帧
     */
    List<BufferedImage> imagesLeft;


    /**
     * 向右攻击的图片动画帧
     */
    List<BufferedImage> attackRight = new ArrayList<BufferedImage>() {
        {
            add(Utils.loading("ikun/r4.png"));
        }
    };

    /**
     * 向左攻击的图片动画帧
     */
    List<BufferedImage> attackLeft = new ArrayList<BufferedImage>() {
        {
            add(Utils.loading("ikun/l4.png"));
        }
    };


    /**
     * 篮球列表
     */
    public ConcurrentLinkedQueue<Ball> balls = new ConcurrentLinkedQueue<>();


    public Hero(GamePanel gamePanel) {
        super(gamePanel);
    }


    @Override
    public void init() {

        imagesRight = new ArrayList<BufferedImage>() {
            {
                add(Utils.loading("ikun/r1.png"));
                add(Utils.loading("ikun/r2.png"));
                add(Utils.loading("ikun/r3.png"));
            }
        };

        imagesLeft = new ArrayList<BufferedImage>() {
            {
                add(Utils.loading("ikun/l1.png"));
                add(Utils.loading("ikun/l2.png"));
                add(Utils.loading("ikun/l3.png"));
            }
        };

        setImages(imagesRight);
        setImageHeight(getImg().getHeight() * 3 / 2);
        setImageWidth(getImg().getWidth() * 3 / 2);

    }

    @Override
    public void update() {

    }


    @Override
    public void destroy() {

    }

    /**
     * 向左移动
     */
    public void moveLeft(){

        direction = false;

        if (images != imagesLeft){
            setImages(imagesLeft);
        }

        x -= PLAYER_SPEED;

        if (getX() < -getImageWidth()){

            setX(Config.FRAME_WIDTH + getImageWidth());
            //切换上一张地图
            getGamePanel().prevMap();
        }

    }

    /**
     * 向右移动
     */
    public void moveRight(){

        direction = true;

        if (images != imagesRight){
            setImages(imagesRight);
        }

        setX(getX() + PLAYER_SPEED);

        if (getX() > Config.FRAME_WIDTH + getImageWidth()){
            setX(-getImageWidth());
            //切换下一张地图
            getGamePanel().nextMap();
        }

    }

    /**
     * 向上移动
     */
    public void moveUp(){
        setY(getY() - PLAYER_SPEED);

        if (getY() < -getImageHeight()){
            setY(Config.FRAME_HEIGHT + getImageHeight());
        }
    }


    /**
     * 向下移动
     */
    public void moveDown(){

        setY(getY() + PLAYER_SPEED);

        if (getY() > Config.FRAME_HEIGHT + getImageHeight()){
            setY(-getImageHeight());
        }

    }


    /**
     * 用户释放大招
     */
    public void releaseSkill() {

        if (mp < PLAYER_MP_MAX){
            return;
        }

        //减少怒气值
        setMp(0);


        //循环创建36个篮球，每个篮球的角度相差10度
        for (int i = 0; i < BASKETBALL_NUM; i++) {

            double radius = 360/(double)BASKETBALL_NUM * i;

            Ball ball = new Ball(getGamePanel(),radius,getX(),getY(),Hero.this);
            balls.add(ball);
            //切换到攻击的图片动画帧
            setImages(direction ? attackRight : attackLeft);

            new Thread(ball).start();
        }


        //等待一会切换回原来的图片动画帧
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                setImages(direction ? imagesRight : imagesLeft);
            }
        };
        timer.schedule(timerTask, 100);



        MusicPlayer musicPlayer;

        try {
            musicPlayer = new MusicPlayer(
                    SKILL_MUSIC,
                    false,
                    0.5f
            );

            musicPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    /**
     * 用户攻击行为
     */
    public void attack(){

        Ball ball = new Ball(getGamePanel(),direction,getX(),getY(),Hero.this);
        balls.add(ball);

        //切换到攻击的图片动画帧
        setImages(direction ? attackRight : attackLeft);

        new Thread(ball).start();

        //切换回原来的图片动画帧
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                setImages(direction ? imagesRight : imagesLeft);
            }
        };
        timer.schedule(timerTask, 100);

        MusicPlayer musicPlayer;

        try {
            musicPlayer = new MusicPlayer(
                ATTACK_MUSIC,
                false,
                0.5f
            );

            musicPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO 周围的敌人受到攻击

    }


    /**
     * 玩家控制行为
     * <ul>
     *     <li>w:上</li>
     *     <li>s:下</li>
     *     <li>a:左</li>
     *     <li>d:右</li>
     *     <li>j:攻击</li>
     *     <li>l:防御</li>
     * </ul>
     * @param code 键盘按键的编码
     */
    public void control(char code){

        switch (code){
            case 'W':
            case 'w':
                moveUp();
                break;
            case 'S':
            case 's':
                moveDown();
                break;
            case 'A':
            case 'a':
                moveLeft();
                break;
            case 'D':
            case 'd':
                moveRight();
                break;
            case 'J':
            case 'j':
                attack();
                break;
            case 'i':
            case 'I':
                releaseSkill();
                break;

        }

        getGamePanel().props.forEach(prop ->{


            //吃药
            if (prop.isCollision(this)){

                switch (prop.type){
                    case 0:
                        //鸡腿加血
                        this.setHp(getHp() + CHICKEN_VALUE);
                        break;

                    case 1:
                        //汤加蓝
                        this.setMp(getMp() + SOUP_VALUE);
                        break;

                }

                getGamePanel().props.remove(prop);

            }

        });


    }

    /**
     * 绘制血量和蓝量
     * @param g 画笔
     */
    public void drawHpAndMp(Graphics g){

        BufferedImage head = Utils.loading("ikun/head.png");

        g.drawImage(
                head,
                10,
                10,
                80,
                80,
                null
        );

        g.setColor(Color.WHITE);
        g.drawRect(90, 20, HP_MP_BAR_MAX_WIDTH, 20);
        g.drawRect(90, 50, HP_MP_BAR_MAX_WIDTH, 20);

        //绘制血量
        g.setColor(Color.RED);
        g.fillRect(90, 20, (int)(HP_MP_BAR_MAX_WIDTH * getHp() / PLAYER_HP), 20);

        //绘制蓝量
        g.setColor(Color.BLUE);
        g.fillRect(90, 50, (int)(HP_MP_BAR_MAX_WIDTH * getMp() / PLAYER_MP_MAX), 20);

    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        drawHpAndMp(g);

        drawAnger(g);

    }

    /**
     * 怒气值满时，玩家周围出现金色的光圈
     * @return
     */
    public void drawAnger(Graphics g){

        if (mp < PLAYER_MP_MAX){
            return;
        }

        BufferedImage angerImage = Utils.loading("boom.gif");

        //设置透明度
        Graphics2D g2d = (Graphics2D) g;

        g.drawImage(
                angerImage,
                getX() - angerImage.getWidth()/2,
                getY() - angerImage.getHeight()/2,
                angerImage.getWidth()*2,
                angerImage.getHeight()*2,
                null
        );

    }



    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;

        if (hp <= 0){
            //TODO 玩家死亡
            System.out.println("玩家死亡");
        }

        if (hp > PLAYER_HP){
            this.hp = PLAYER_HP;
        }

    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;

        if (mp < 0){
            this.mp = 0;
        }

        if (mp >= PLAYER_MP_MAX){
            this.mp = PLAYER_MP_MAX;
        }

    }




}
