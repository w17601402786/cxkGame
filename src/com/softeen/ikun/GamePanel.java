package com.softeen.ikun;

import com.softeen.ikun.model.Boss;
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


    private String[] maps = {
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


    /**
     * 已经通过的地图
     */
    private boolean[] throughMap;

    /**
     * 提示文字
     */
    private String tipText;

    public int mapIndex = 0;


    public Hero hero = new Hero(GamePanel.this);


    public CopyOnWriteArrayList<Enemy> enemies = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Prop> props = new CopyOnWriteArrayList<>();

    /**
     * 大招的gif图
     */
    public ImageIcon imageIcon;

    /**
     * 当前是否已经暂停
     */
    public boolean pause = false;


    /**
     * BOSS
     */
    public Boss boss;


    GamePanel(){

        //开始时没有通过任何地图
        throughMap = new boolean[maps.length];
        Arrays.fill(throughMap, false);

        setBackground(Color.GREEN);

        //添加键盘监听器
        addKeyListener(new KeyAdapter() {
            private HashSet<Character> pressedKeys = new HashSet<>();

            @Override
            public void keyPressed(KeyEvent e) {


                if (e.getKeyChar() == ' '){
                    setPause(!pause);
                }

                //添加到按下的键集合
                pressedKeys.add(e.getKeyChar());

                pressedKeys.forEach(key->{
                    if (!pause){
                        hero.control(key);
                    }
                });

            }

            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyChar() == 'u' || e.getKeyChar() == 'U'){
                    if (pressedKeys.contains('w') || pressedKeys.contains('W')){
                        hero.releaseSkill2();
                    }
                }

                //从按下的键集合中移除
                pressedKeys.remove(e.getKeyChar());

            }
        });

        timer.schedule(updateHeroTask, 0, 100);
        generateEnemyTimer.schedule(generateEnemyTask, 0, 1000);

        //设置焦点
        setFocusable(true);

    }




    /**
     * 更新玩家图像
     */
    TimerTask updateHeroTask = new TimerTask() {
        @Override
        public void run() {
            hero.updateImage();
            enemies.forEach(enemy-> enemy.updateImage());
            if (boss != null) {
                boss.updateImage();
            }
            repaint();
        }
    };

    /**
     * 生成敌人
     */
    TimerTask generateEnemyTask = new TimerTask() {
        @Override
        public void run() {

            if (pause){
                return;
            }


            //如果击败的敌人数大于等于进入下一关的敌人数，则杀死当前页面的所有敌人,不再生成，并开放下一关
            if (killCount >= MAX_ENEMY_COUNT * (mapIndex + 1)) {
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
    public Timer generateEnemyTimer = new Timer();


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

        //当前地图已经通过
        throughMap[mapIndex] = true;

        mapIndex++;

        //绘制文字显示当前第几关
        tipText = "第" + (mapIndex + 1) + "关";

        //隔一段时间后消失，即加一个定时器将其置空
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                tipText = null;
            }
        }, 3000);



        //TODO boss关卡放到后面
        if (mapIndex ==2){

            //最后一关，不需要生成敌人
            killCount = 0x7fffffff;

            allowNextMap = false;

            generateEnemyTimer.cancel();
            enemies.forEach(enemy -> enemy.kill());

            generateBoss();
        }


    }

    /**
     * 创建BOSS
     */
    private void generateBoss() {
        //生成BOSS
        boss = new Boss(GamePanel.this);
        new Thread(boss).start();
    }


    @Override
    public void paint(Graphics g) {

        super.paint(g);

        Image img = Utils.loading(maps[mapIndex]);
        g.drawImage(img, 0, 0, FRAME_WIDTH, FRAME_HEIGHT, null);

        //正在释放技能
        if (imageIcon!=null){
            g.drawImage(imageIcon.getImage(),0,0,FRAME_WIDTH,FRAME_HEIGHT,null);
        }

        //绘制BOSS
        if (boss!=null && !boss.isDeath()){
            boss.draw(g);
        }

        hero.draw(g);
        hero.balls.forEach(ball -> ball.draw(g));
        enemies.forEach(enemy -> enemy.draw(g));
        props.forEach(prop -> prop.draw(g));

        if (pause){
            // 设置字体颜色和字体大小
            g.setColor(Color.WHITE);
            g.setFont(new Font("宋体", Font.BOLD, 50));
            // 获取提示信息的宽度
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth("游戏暂停，按空格键继续游戏");
            // 居中显示提示信息
            int x = (FRAME_WIDTH - textWidth) / 2;
            int y = FRAME_HEIGHT / 2;
            // 绘制提示信息
            g.drawString("游戏暂停，按空格键继续游戏", x, y);
        }

        if (tipText!=null){

            // 设置字体颜色和字体大小
            g.setColor(Color.WHITE);
            g.setFont(new Font("宋体", Font.BOLD, 50));
            // 获取提示信息的宽度
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(tipText);
            // 居中显示提示信息
            int x = (FRAME_WIDTH - textWidth) / 2;
            int y = FRAME_HEIGHT / 4;
            // 绘制提示信息
            g.drawString(tipText, x, y);


        }



    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
}
