package com.softeen.ikun;

import com.softeen.ikun.model.Enemy;
import com.softeen.ikun.model.Hero;
import com.softeen.ikun.model.Prop;
import com.softeen.ikun.tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.softeen.ikun.Config.*;

public class GamePanel extends JPanel {





    //消灭敌人的数量
    public int killCount = 0;

    private boolean allowNextMap = false;


    String[] maps = {
            "bg0.jpg",
            "bg1.jpg",
            "bg2.jpg",
            "bg3.jpg",
            "bg4.png",
            "bg5.png",
            "bg6.png",
            "bg7.png",
            "bg8.png",
    };

    public int mapIndex = 0;


    public Hero hero = new Hero(GamePanel.this);


    public CopyOnWriteArrayList<Enemy> enemies = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Prop> props = new CopyOnWriteArrayList<>();

    /**
     * 更新玩家图像
     */
    TimerTask updateHeroTask = new TimerTask() {
        @Override
        public void run() {
            hero.updateImage();
            enemies.forEach(enemy-> enemy.updateImage());

            repaint();
        }
    };

    /**
     * 生成敌人
     */
    TimerTask generateEnemyTask = new TimerTask() {
        @Override
        public void run() {

            //如果击败的敌人数大于等于进入下一关的敌人数，则杀死当前页面的所有敌人,不再生成，并开放下一关
            if (killCount >= MAX_ENEMY_COUNT) {
                allowNextMap = true;
                enemies.forEach(enemy -> enemy.kill());
                return;
            }

            Enemy enemy = new Enemy(GamePanel.this,enemies);
            new Thread(enemy).start();
            enemies.add(enemy);
        }
    };

    public Timer timer = new Timer();


    /**
     * 上一张地图
     */
    public void prevMap(){

        if(mapIndex <= 0){
            return;
        }

        mapIndex--;

    }

    /**
     * 下一张地图
     */
    public void nextMap(){

        if(mapIndex >= maps.length - 1){
            return;
        }

        if (!allowNextMap) {
            return;
        }

        killCount = 0;
        allowNextMap = false;

        enemies.forEach(enemy -> enemy.kill());

        mapIndex++;


        if (mapIndex == maps.length - 1) {
            //TODO 进入BOSS关卡
        }


    }


    GamePanel(){

        setBackground(Color.GREEN);

        //添加键盘监听器
        addKeyListener(new KeyAdapter() {
            private HashSet<Character> pressedKeys = new HashSet<>();

            @Override
            public void keyPressed(KeyEvent e) {


                //添加到按下的键集合
                pressedKeys.add(e.getKeyChar());

                pressedKeys.forEach(key->{
                    hero.control(key);
                });

            }

            @Override
            public void keyReleased(KeyEvent e) {
                //从按下的键集合中移除
                pressedKeys.remove(e.getKeyChar());
            }
        });

        timer.schedule(updateHeroTask, 0, 100);
        timer.schedule(generateEnemyTask, 0, 1000);

        //设置焦点
        setFocusable(true);

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Image img = Utils.loading(maps[mapIndex]);
        g.drawImage(img, 0, 0, FRAME_WIDTH, FRAME_HEIGHT, null);

        hero.draw(g);
        hero.balls.forEach(ball -> ball.draw(g));
        enemies.forEach(enemy -> enemy.draw(g));

    }



}
