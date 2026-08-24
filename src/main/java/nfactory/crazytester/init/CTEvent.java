package nfactory.crazytester.init;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nfactory.crazytester.CrazyTester;
import nfactory.crazytester.command.FishingCommand;
import nfactory.crazytester.command.TreasureCommand;
import nfactory.crazytester.test.FishingTest;
import nfactory.crazytester.test.TreasureTest;

@Mod.EventBusSubscriber(modid = CrazyTester.MODID)
public class CTEvent {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TreasureCommand.register(event.getDispatcher());
        FishingCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        TreasureTest.tick();
        FishingTest.tick();
    }
}
