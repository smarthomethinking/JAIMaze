package org.smarthomethinking.jaimaze;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import org.smarthomethinking.jaimaze.server.History;
import org.smarthomethinking.jaimaze.server.ClientResponse;
import org.smarthomethinking.jaimaze.server.ClientRequest;
import org.smarthomethinking.jaimaze.server.Position;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author gde
 */
public class MazeRunnerTask extends Thread {

    private static final Logger LOG = Logger.getLogger(MazeRunnerTask.class.getName());
    private final Model model;
    private final UiController controller;
    private final int width;
    private final int height;
    private boolean running = false;

    private SquareType[][] grid;
    private int startX, startY;
    private int positionX, positionY;
    private final String boardID;

    private static final double REWARD_EXIT = +1000.0;
    private static final double REWARD_WALK_INTO_WALL = -10.0;
    private static final double REWARD_WALK_OFF_EDGE = -20.0;
    private static final double REWARD_STEP = -0.1;
    private static final double REWARD_TRAP = -100.0;

    // information to draw the current snapshot.
    private BufferedImage map;
    private int mapScale = 1;

    public MazeRunnerTask(Model model,UiController con) {
        this.controller = con;
        this.model = model;
        width = model.getWidth().get();
        height = model.getHeight().get();
        boardID = UUID.randomUUID().toString();
        LOG.log(Level.INFO, "Initialising board {0} width={1} height={2}", new Object[]{boardID, width, height});
    }

    public void stopThread() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        LOG.info("Starting run thread");
        Random rand = new Random();
        LOG.info("Initialising the grid");
        initialiseGrid(rand);
        History lastMove = null;
        positionX = startX;
        positionY = startY;
        while (running) {
            // work out request
            ClientRequest request = new ClientRequest();
            // populate the config
            request.getConfig().setBoardID(boardID);
            request.getConfig().setWidth(width);
            request.getConfig().setHeight(height);
            if (lastMove != null) {
                request.setHistory(lastMove);
            }
            request.setCurrentPosition(new Position(positionX, positionY));
            // send and get response
            RestTemplate restTemplate = new RestTemplate();
            try {
                ClientResponse response = restTemplate.postForObject(new URI(model.getUrl().get()), request, ClientResponse.class);
                LOG.info("Recieved response from server " + response.toString());
                lastMove = new History();
                lastMove.setLastPosition(new Position(positionX, positionY));
                lastMove.setAction(response.getMove());
                // move the agent
                switch (response.getMove()) {
                    case "North":
                        positionY--;
                        break;
                    case "South":
                        positionY++;
                        break;
                    case "East":
                        positionX++;
                        break;
                    case "West":
                        positionX--;
                        break;
                    default:
                        LOG.warning("Invalid move made '" + response.getMove() + "'");
                }
                LOG.info("Moved to " + positionX + "," + positionY);
                lastMove.setNewPosition(new Position(positionX,positionY));
                // work out the reward
                if ((positionX < 0) || (positionX >= width) || (positionY < 0) || (positionY >= height)) {
                    // we've walked off the edge - reset the position to the start
                    LOG.info("Walked off edge - resetting");
                    positionX = startX;
                    positionY = startY;
                    lastMove.setNewPosition(null);
                    lastMove.setReward(REWARD_WALK_OFF_EDGE);
                } else if (grid[positionX][positionY] == SquareType.EXIT) {
                    // we've found the exit - reset back to the start
                    LOG.info("Found exit - resetting");
                    positionX = startX;
                    positionY = startY;
                    lastMove.setNewPosition(null);
                    lastMove.setReward(REWARD_EXIT);
                } else {
                    // we've taken a step, but nothing interesting has happened
                    lastMove.setReward(REWARD_STEP);
                }
                // redraw the map
                BufferedImage image = deepCopy(map);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.BLACK);
                g.fillOval(positionX * mapScale, positionY * mapScale, mapScale, mapScale);
                // update UI
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.updateBoardImage(image);
                    }
                });
            } catch (URISyntaxException ex) {
                Logger.getLogger(MazeRunnerTask.class.getName()).log(Level.SEVERE, "Error talking to server", ex);
                running = false;
            }
        }
        LOG.info("Finished run thread");
        model.getUIEnabled().set(true);
    }

    private void initialiseGrid(Random rand) {
        LOG.log(Level.INFO, "Initialising maze width={0} height={1}", new Object[]{width, height});
        grid = new SquareType[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = SquareType.SPACE;
            }
        }
        // create random start and finish spaces
        startX = rand.nextInt(width);
        startY = rand.nextInt(height);
        LOG.log(Level.INFO, "Set starting position to {0},{1}", new Object[]{startX, startY});
        int goalX, goalY;
        do {
            goalX = rand.nextInt(width);
            goalY = rand.nextInt(height);
        } while ((goalX == startX) && (goalY == startY));
        LOG.log(Level.INFO, "Set goal position to {0},{1}", new Object[]{goalX, goalY});
        grid[goalX][goalY] = SquareType.EXIT;
        // work out the scale of the map
        mapScale = 20;
        // create base map
        map = new BufferedImage(width * mapScale+1, height * mapScale+1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = map.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width*mapScale, height*mapScale);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                g.setColor(Color.darkGray);
                g.drawRect(x * mapScale, y * mapScale, mapScale, mapScale);
                switch (grid[x][y]) {
                    case EXIT:
                        g.setColor(Color.yellow);
                        g.fillRect(x * mapScale + 3, y * mapScale + 3, mapScale - 6, mapScale - 6);
                        break;
                    case TRAP:
                        g.setColor(Color.red);
                        g.fillRect(x * mapScale + 1, y * mapScale + 1, mapScale - 2, mapScale - 2);
                        break;
                    case WALL:
                        g.setColor(Color.lightGray);
                        g.fillRect(x * mapScale + 1, y * mapScale + 1, mapScale - 2, mapScale - 2);
                        break;
                }
            }
        }
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

}
