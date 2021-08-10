
package com.codinglitch.venerations.event;

import com.codinglitch.venerations.CTweaks;
import com.codinglitch.venerations.registry.capabilities.DeathFearProvider;
import com.codinglitch.venerations.registry.capabilities.IDeathFear;
import com.codinglitch.venerations.util.ReferenceC;
import com.codinglitch.venerations.util.network.CTweaksPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.overlay.DebugOverlayGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Random;

@EventBusSubscriber
public class CommonEventHandler {
    public static Random rand = new Random();

    @SubscribeEvent
    public static void onCrit(CriticalHitEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity & event.getTarget() instanceof LivingEntity & event.isVanillaCritical())
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();

            event.setDamageModifier(Math.min(event.getDamageModifier()+player.fallDistance/10, 2.2f));

            System.out.println(((LivingEntity) event.getTarget()).getHealth());
        }
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof PlayerEntity)) return;

        event.addCapability(new ResourceLocation(ReferenceC.MODID, "deathfear"), new DeathFearProvider());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        LazyOptional<IDeathFear> capability = event.getEntity().getCapability(DeathFearProvider.capability);

        if (capability.isPresent())
        {
            IDeathFear cap = capability.orElseThrow(IllegalArgumentException::new);

            String death = event.getSource().msgId;

            if (death.equals("inFire") | death.equals("onFire") | death.equals("lava"))
            {
                death = "fire";
            }
            if (death.equals("mob"))
            {
                death += event.getSource().getEntity().getName().getString().toLowerCase();
            }
            if (event.getSource().isExplosion())
            {
                death = "explosion" + event.getSource().getEntity().getName().getString().toLowerCase();
            }

            cap.setFear(death);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath()) return;
        PlayerEntity from = event.getOriginal();
        PlayerEntity to = event.getPlayer();

        String fear = "";

        // Original

        LazyOptional<IDeathFear> capability = from.getCapability(DeathFearProvider.capability);

        if (capability.isPresent())
        {
            IDeathFear cap = capability.orElseThrow(IllegalArgumentException::new);
            fear = cap.getFear();
        }

        // Cloned

        LazyOptional<IDeathFear> capability1 = to.getCapability(DeathFearProvider.capability);

        if (capability1.isPresent())
        {
            IDeathFear cap = capability1.orElseThrow(IllegalArgumentException::new);
            cap.setFear(fear);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        LazyOptional<IDeathFear> capability = player.getCapability(DeathFearProvider.capability);

        if (capability.isPresent())
        {
            IDeathFear cap = capability.orElseThrow(IllegalArgumentException::new);

            String fear = cap.getFear();

            if (fear.equals("arrow"))
            {
                if (event.getSource().msgId.equals("arrow"))
                {
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 2));
                    cap.setFearCounter(200);
                    cap.setMaxFearCounter(200);
                }
            }
            else if (fear.startsWith("mob"))
            {
                if (event.getSource().msgId.equals("mob"))
                {
                    player.addEffect(new EffectInstance(Effects.WEAKNESS, 40, 2));
                    cap.setFearCounter(200);
                    cap.setMaxFearCounter(200);
                    player.disableShield(true);
                }

                String mob = fear.substring(3);

            }
        }
    }

    @SubscribeEvent
    public static void onSleep(SleepingLocationCheckEvent event)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        LazyOptional<IDeathFear> capability = player.getCapability(DeathFearProvider.capability);

        if (capability.isPresent()) {
            IDeathFear cap = capability.orElseThrow(IllegalArgumentException::new);
            String fear = cap.getFear();

            if (cap.getFearCounter() > 0) {
                cap.setFearCounter(cap.getFearCounter() - 1);
            }

            if (fear.equals("outOfWorld")) {
                cap.setFearCounter(60);
                cap.setMaxFearCounter(60);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onChat(ClientChatEvent event)
    {
        //event.setMessage(event.getMessage().replaceAll("", "\\u00A7ka"));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;



        LazyOptional<IDeathFear> capability = player.getCapability(DeathFearProvider.capability);

        if (capability.isPresent())
        {
            IDeathFear cap = capability.orElseThrow(IllegalArgumentException::new);
            String fear = cap.getFear();

            if (cap.getFearCounter() > 0)
            {
                cap.setFearCounter(cap.getFearCounter()-1);
            }

            if (fear.equals("fall"))
            {

                if (player.position().y > 100)
                {
                    cap.setFearCounter(50);
                    cap.setMaxFearCounter(50);
                    if (player.tickCount % 40 == 0)
                    {
                        player.push((rand.nextFloat()-0.5f)*0.5f,rand.nextFloat()*0.25f,(rand.nextFloat()-0.5f)*0.5f);
                    }
                }
            }
            else if (fear.equals("drown"))
            {
                if (player.isUnderWater())
                {
                    if (player.tickCount % 2 == 0)
                    {
                        player.setAirSupply(player.getAirSupply()-1);
                    }
                }
            }
            else if (fear.startsWith("explosion"))
            {
                String mob = fear.substring(9);

                if (mob.equals("creeper"))
                {
                    if (rand.nextInt(4000) == 1)
                    {
                        cap.setFearCounter(100);
                        cap.setMaxFearCounter(100);
                        player.level.playSound(player, new BlockPos(player.position()), SoundEvents.CREEPER_PRIMED, SoundCategory.AMBIENT, 1, 1);
                    }
                }
            }
            else if (fear.equals("fire"))
            {
                loop:
                {
                    if (player.tickCount % 40 == 0)
                    {
                        int X = (int) player.position().x;
                        int Y = (int) player.position().y;
                        int Z = (int) player.position().z;
                        int range = 5;

                        Block block = player.level.getBlockState(new BlockPos(player.pick(20, 0, false).getLocation())).getBlock();

                        if (block == Blocks.FIRE)
                        {
                            cap.setFearCounter(200);
                            cap.setMaxFearCounter(200);
                            player.addEffect(new EffectInstance(Effects.WEAKNESS, 50, 1));
                            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 50, 2));
                            break loop;
                        }

                        for (int x = X-range; x < X+range; x++) {
                            for (int y = Y-range; y < Y+range; y++) {
                                for (int z = Z-range; z < Z+range; z++) {
                                    BlockPos pos = new BlockPos(x,y,z);
                                    if (player.level.getBlockState(pos).getBlock() == Blocks.LAVA)
                                    {
                                        cap.setFearCounter(200);
                                        cap.setMaxFearCounter(200);
                                        player.addEffect(new EffectInstance(Effects.WEAKNESS, 50, 1));
                                        player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 50, 2));
                                        break loop;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        if (event.side == LogicalSide.SERVER)
        {
            if (player.tickCount % 40 == 0)
            {
                LazyOptional<IDeathFear> capability1 = player.getCapability(DeathFearProvider.capability);

                if (capability1.isPresent())
                {
                    IDeathFear cap = capability1.orElseThrow(IllegalArgumentException::new);
                    CTweaksPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CTweaksPacketHandler.SyncFear(cap.getFear(), cap.getFearCounter(), cap.getMaxFearCounter()));
                }
            }
        }
    }
}
