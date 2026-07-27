package gdd;

import gdd.scene.EndingScene;
import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.SceneTransition;
import gdd.scene.TitleScene;
import javax.swing.JFrame;

public class Game extends JFrame  {

    private final TitleScene titleScene;
    private Scene1 scene1;
    private SceneTransition sceneTransition;
    private Scene2 scene2;
    private EndingScene endingScene;

    public Game() {
        titleScene = new TitleScene(this);
        initUI();
        // loadTitle();
        loadScene1();
    }

    private void initUI() {

        setTitle("Space Invaders");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

    }

    public void loadTitle() {
        stopActiveScenes();
        getContentPane().removeAll();
        add(titleScene);
        titleScene.start();
        refreshScene();
    }

    public void loadScene1() {
        stopActiveScenes();
        scene1 = new Scene1(this);
        getContentPane().removeAll();
        add(scene1);
        scene1.start();
        refreshScene();
    }

    /**
     * Member 1 should call this after miniBossKills reaches 30.
     */
    public void loadSceneTransition() {
        stopActiveScenes();
        sceneTransition = new SceneTransition(this);
        getContentPane().removeAll();
        add(sceneTransition);
        sceneTransition.start();
        refreshScene();
    }

    public void loadScene2() {
        stopActiveScenes();
        scene2 = new Scene2(this);
        getContentPane().removeAll();
        add(scene2);
        scene2.start();
        refreshScene();
    }

    public void loadEndingScene() {
        stopActiveScenes();
        endingScene = new EndingScene(this);
        getContentPane().removeAll();
        add(endingScene);
        endingScene.start();
        refreshScene();
    }

    private void stopActiveScenes() {
        titleScene.stop();
        if (scene1 != null) {
            scene1.stop();
        }
        if (sceneTransition != null) {
            sceneTransition.stop();
        }
        if (scene2 != null) {
            scene2.stop();
        }
        if (endingScene != null) {
            endingScene.stop();
        }
    }

    private void refreshScene() {
        revalidate();
        repaint();
    }
}
