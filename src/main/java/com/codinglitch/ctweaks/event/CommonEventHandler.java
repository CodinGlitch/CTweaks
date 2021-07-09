
package com.codinglitch.ctweaks.event;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.config.DisableConfig;
import com.codinglitch.ctweaks.registry.capabilities.DeathFearProvider;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.codinglitch.ctweaks.registry.entities.PolarBearEntityModified;
import com.codinglitch.ctweaks.registry.init.EntityInit;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.UtilityC;
import com.codinglitch.ctweaks.util.network.CTweaksPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@EventBusSubscriber
public class CommonEventHandler {
    public static Random rand = new Random();

    public static List<Pair<UUID, String>> costList = new ArrayList<>();

    public static void setCost(PlayerEntity player, String type)
    {
        removeCost(player.getUUID());
        costList.add(Pair.of(player.getUUID(),type));
    }

    public static String getCost(UUID player)
    {
        for (Pair<UUID, String> pair : costList)
        {
            if (pair.getKey().equals(player))
                return pair.getValue();
        }

        return "";
    }

    public static void removeCost(UUID player)
    {
        costList.removeIf((pair) -> pair.getKey().equals(player));
    }

    @SubscribeEvent
    public static void onRepair(AnvilRepairEvent event)
    {
        int cost = UtilityC.getLevelCostFromItems(event.getItemInput(), event.getIngredientInput());;
        if (cost != 0)
        {
            setCost(event.getPlayer(), "anvil");
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        removeCost(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event)
    {
        if (event.getContainer() instanceof EnchantmentContainer)
        {
            removeCost(event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock event)
    {
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState blockState = event.getWorld().getBlockState(pos);
        TileEntity tileEntity = event.getWorld().getBlockEntity(pos);
        if (tileEntity instanceof EnchantingTableTileEntity)
        {
            setCost(event.getPlayer(), "enchant");
        }
        else if (blockState.getBlock() instanceof CauldronBlock)
        {
            if (event.getPlayer().isCrouching()) return;
            int i = blockState.getValue(CauldronBlock.LEVEL);
            PlayerEntity player = event.getPlayer();
            World world = player.level;
            Hand hand = event.getHand();
            ItemStack itemstack = event.getPlayer().getItemInHand(hand);
            CauldronBlock block = (CauldronBlock) blockState.getBlock();

            if (itemstack.getItem() == Items.SPONGE)
            {
                if (i == 3) {
                    event.setCanceled(true);
                    if (!world.isClientSide)
                    {
                        if (!player.abilities.instabuild) {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                player.setItemInHand(hand, new ItemStack(Items.WET_SPONGE));
                            } else if (!player.inventory.add(new ItemStack(Items.WET_SPONGE))) {
                                player.drop(new ItemStack(Items.WET_SPONGE), false);
                            }
                        }

                        player.awardStat(Stats.USE_CAULDRON);
                        block.setWaterLevel(world, pos, blockState, 0);
                        world.playSound(null, pos, SoundsC.sponge.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }

                }
            }
            else if (itemstack.getItem() == Items.WET_SPONGE)
            {
                if (i < 3) {
                    event.setCanceled(true);
                    if (!world.isClientSide)
                    {
                        if (!player.abilities.instabuild) {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                player.setItemInHand(hand, new ItemStack(Items.SPONGE));
                            } else if (!player.inventory.add(new ItemStack(Items.SPONGE))) {
                                player.drop(new ItemStack(Items.SPONGE), false);
                            }
                        }

                        player.awardStat(Stats.FILL_CAULDRON);
                        block.setWaterLevel(world, pos, blockState, 3);
                        world.playSound(null, pos, SoundsC.wet_sponge.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelChange(PlayerXpEvent.LevelChange event)
    {
        String cost = getCost(event.getPlayer().getUUID());
        if (!cost.equals(""))
        {
            if (cost.equals("anvil"))
                removeCost(event.getPlayer().getUUID());

            event.setCanceled(true);
            event.getPlayer().giveExperiencePoints(UtilityC.getExperienceFromLevel(event.getLevels()));
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof FoxEntity)
        {
            if (DisableConfig.tame_fox.get()) return;
            event.setCanceled(true);
            FoxEntityModified fox = new FoxEntityModified(EntityInit.FOX_MODIFIED.get(), event.getWorld());
            fox.copyPosition(event.getEntity());
            event.getWorld().addFreshEntity(fox);
        }
        else if (event.getEntity().getClass() == PolarBearEntity.class)
        {
            if (DisableConfig.mount_polar_bear.get()) return;
            event.setCanceled(true);
            PolarBearEntityModified bear = new PolarBearEntityModified(EntityInit.POLAR_BEAR_MODIFIED.get(), event.getWorld());
            bear.copyPosition(event.getEntity());
            bear.setBaby(((PolarBearEntity)event.getEntity()).isBaby());
            event.getWorld().addFreshEntity(bear);
        }
    }

    @SubscribeEvent
    public static void onCrit(CriticalHitEvent event)
    {
        if (DisableConfig.axe_crit.get()) return;

        if (event.getEntity() instanceof PlayerEntity & event.getTarget() instanceof LivingEntity & event.isVanillaCritical())
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();

            event.setDamageModifier(Math.min(event.getDamageModifier()+player.fallDistance/10, 2.2f));

            System.out.println(((LivingEntity) event.getTarget()).getHealth());
        }
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (DisableConfig.trauma.get()) return;

        if (!(event.getObject() instanceof PlayerEntity)) return;

        event.addCapability(new ResourceLocation(ReferenceC.MODID, "deathfear"), new DeathFearProvider());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {
        if (DisableConfig.trauma.get()) return;

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
            if (event.getSource().isExplosion() & event.getSource().getEntity() != null)
            {
                death = "explosion" + event.getSource().getEntity().getName().getString().toLowerCase();
            }

            cap.setFear(death);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event)
    {
        if (DisableConfig.trauma.get()) return;

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
    public static void onLivingAttack(LivingAttackEvent event)
    {
        if (event.getEntityLiving() instanceof FoxEntityModified) // Have to make foxes immune to berry bushes here because .immuneTo is not working (or im doing something wrong)
        {
            if (event.getSource() == DamageSource.SWEET_BERRY_BUSH)
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event)
    {
        if (DisableConfig.trauma.get()) return;

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
        if (DisableConfig.trauma.get()) return;

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
        if (DisableConfig.trauma.get()) return;

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
                        cap.setFearCounter(200);
                        cap.setMaxFearCounter(200);
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
