import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import life.lluis.multiversehardcore.MultiverseHardcore;
import life.lluis.multiversehardcore.exceptions.HardcoreWorldCreationException;
import life.lluis.multiversehardcore.models.HardcoreWorld;
import life.lluis.multiversehardcore.models.HardcoreWorldConfiguration;
import org.bukkit.GameMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.MockMVWorldManager;
import utils.MockWorldCreator;
import utils.TestUtils;

import java.util.Date;

public class SpectatorModeDeathBanTest {

    private ServerMock server;
    private MockWorldCreator mockWorldCreator;

    @Before
    public void setUp() {
        server = MockBukkit.mock();
        MultiverseHardcore plugin = MockBukkit.load(MultiverseHardcore.class);
        MockMVWorldManager worldManager = new MockMVWorldManager(server);
        plugin.setMVWorldManager(worldManager);
        mockWorldCreator = new MockWorldCreator(server, worldManager);
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testGameModeIsSetToSpectatorWhenJoiningWorld() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, true, 0, true, true, true));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    public void testGameModeIsNotSetToSpectatorWhenJoiningWorldOP() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), false, true, 0, true, true, true));
        PlayerMock player = TestUtils.addOP(server); // Join world once is set to hardcore
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    public void testGameModeIsSetToSpectatorWhenJoiningWorldOP() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, true, 0, true, true, true));
        PlayerMock player = TestUtils.addOP(server); // Join world once is set to hardcore
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    public void testGameModeIsSetToSurvivalWhenDeathBanEnds() throws HardcoreWorldCreationException, InterruptedException {
        final int banLengthInSeconds = 1;
        WorldMock world = mockWorldCreator.createNormalWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, false, banLengthInSeconds, true, true, true));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.killPlayer(server, player);
        Thread.sleep(banLengthInSeconds * 1000);
        TestUtils.fireJoinEvent(server, player);
        player.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    public void testGameModeIsSetToSurvivalWhenJoiningDifferentWorld() throws HardcoreWorldCreationException {
        WorldMock hcWorld = mockWorldCreator.createNormalWorld();
        WorldMock world = mockWorldCreator.createNormalWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(hcWorld, null, new Date(), true, true, 0, true, true, true));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SPECTATOR);
        TestUtils.teleportPlayer(player, world);
        TestUtils.fireJoinEvent(server, player);
        player.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    public void testGameModeIsSetToSpectatorWhenDyingOnTheNether() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        WorldMock nether = mockWorldCreator.createNetherWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, true, 0, true, true, true));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.teleportPlayer(player, nether);
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    public void testGameModeIsNotSetToSpectatorWhenDyingOnTheNether() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        WorldMock nether = mockWorldCreator.createNetherWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, true, 0, true, false, true));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.teleportPlayer(player, nether);
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    public void testGameModeIsSetToSpectatorWhenDyingOnTheEnd() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        WorldMock theEnd = mockWorldCreator.createTheEndWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, true, 0, true, true, true));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.teleportPlayer(player, theEnd);
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    public void testGameModeIsNotSetToSpectatorWhenDyingOnTheEnd() throws HardcoreWorldCreationException {
        WorldMock world = mockWorldCreator.createNormalWorld();
        WorldMock theEnd = mockWorldCreator.createTheEndWorld();
        HardcoreWorld.createHardcoreWorld(new HardcoreWorldConfiguration(world, null, new Date(), true, true, 0, true, true, false));
        PlayerMock player = server.addPlayer(); // Join world once is set to hardcore
        TestUtils.teleportPlayer(player, theEnd);
        TestUtils.killPlayer(server, player);
        player.assertGameMode(GameMode.SURVIVAL);
    }
}
