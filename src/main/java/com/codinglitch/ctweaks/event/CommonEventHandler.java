
package com.codinglitch.ctweaks.event;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.config.DisableConfig;
import com.codinglitch.ctweaks.registry.capabilities.DeathFearProvider;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import com.codinglitch.ctweaks.registry.entities.FoxEntityCopy;
import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.codinglitch.ctweaks.registry.entities.IllusionerModified;
import com.codinglitch.ctweaks.registry.entities.PolarBearEntityModified;
import com.codinglitch.ctweaks.registry.init.EntityInit;
import com.codinglitch.ctweaks.registry.init.ItemsInit;
import com.codinglitch.ctweaks.registry.items.ScytheItem;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.UtilityC;
import com.codinglitch.ctweaks.util.network.CTweaksPacketHandler;
import net.minecraft.block.*;
import net.minecraft.client.particle.TotemOfUndyingParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.jmx.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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

    public static ItemStack getRotten(ItemStack stack)
    {
        if (stack.getItem() == Items.WHEAT)
            return new ItemStack(ItemsInit.WILTED_WHEAT.get(), stack.getCount());
        if (stack.getItem() == Items.POTATO)
            return new ItemStack(Items.POISONOUS_POTATO, stack.getCount());
        if (stack.getItem() == Items.CARROT)
            return new ItemStack(ItemsInit.CRUMBLING_CARROT.get(), stack.getCount());
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event)
    {
        World level = event.getEntity().level;
        if (level.isClientSide) return;
        BlockPos blockPos = event.getPos();
        BlockState blockState = event.getState();

        ItemStack itemStack = ItemStack.EMPTY;

        if (blockState.getBlock() == Blocks.WHEAT)
        {
            itemStack = new ItemStack(Items.WHEAT);
        }
        else if (blockState.getBlock() == Blocks.POTATOES)
        {
            itemStack = new ItemStack(Items.POTATO);
        }
        else if (blockState.getBlock() == Blocks.CARROTS)
        {
            itemStack = new ItemStack(Items.CARROT);
        }

        if (itemStack.isEmpty()) return;

        boolean isHoldingScythe = event.getPlayer().getMainHandItem().getItem() instanceof ScytheItem;

        TileEntity tileentity = blockState.hasTileEntity() ? level.getBlockEntity(blockPos) : null;

        LootContext.Builder lootcontext = (new LootContext.Builder((ServerWorld)level))
                .withRandom(level.random)
                .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(blockPos))
                .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootParameters.BLOCK_ENTITY, tileentity)
                .withOptionalParameter(LootParameters.THIS_ENTITY, event.getEntityLiving());

        List<ItemStack> drops = blockState.getDrops(lootcontext);

        if (blockState.getBlock() instanceof CropsBlock) {
            CropsBlock cropsblock = (CropsBlock) blockState.getBlock();
            if (!cropsblock.isMaxAge(blockState)) {
                for (ItemStack drop : drops) {
                    level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), drop));
                }
                return;
            }
        }

        int am = 0;

        for (ItemStack drop : drops) {
            if (drop.getItem() == itemStack.getItem())
            {
                am++;
                continue;
            }
            level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), drop));
        }


        for (int o = 0; o < am; o++) {
            int amount = 1;
            if (level.random.nextInt(3)==0 & isHoldingScythe & !DisableConfig.scythes.get()) amount = 2;
            for (int i = 0; i < amount; i++) {
                if (level.random.nextInt(10)==0 & !DisableConfig.rotten_variant.get()) itemStack = getRotten(itemStack);
                level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack));
            }
        }
    }

    @SubscribeEvent
    public static void onHarvest(PlayerEvent.HarvestCheck event)
    {
        boolean cancel = false;

        if (
                event.getTargetBlock().getBlock() == Blocks.WHEAT |
                event.getTargetBlock().getBlock() == Blocks.POTATOES |
                event.getTargetBlock().getBlock() == Blocks.CARROTS
        )
            cancel = true;

        if (cancel) event.setCanHarvest(false);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (DisableConfig.enchantingpatch.get()) return;
        removeCost(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event)
    {
        if (DisableConfig.enchantingpatch.get()) return;
        if (event.getContainer() instanceof EnchantmentContainer | event.getContainer() instanceof RepairContainer)
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
            if (event.getEntity().level.isClientSide) return;
            if (DisableConfig.enchantingpatch.get()) return;
            setCost(event.getPlayer(), "enchant");
        }
        else if (blockState.getBlock() instanceof AnvilBlock)
        {
            if (event.getEntity().level.isClientSide) return;
            if (DisableConfig.enchantingpatch.get()) return;
            setCost(event.getPlayer(), "anvil");
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
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event)
    {
        if (event.getEntity() instanceof HorseEntity)
        {
            HorseEntity entity = (HorseEntity) event.getEntity();
            event.getEntity().remove();
            ZombieHorseEntity zombieHorse = EntityType.ZOMBIE_HORSE.create(event.getEntity().level);
            zombieHorse.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
            zombieHorse.setNoAi(entity.isNoAi());
            zombieHorse.setBaby(entity.isBaby());
            if (entity.hasCustomName()) {
                zombieHorse.setCustomName(entity.getCustomName());
                zombieHorse.setCustomNameVisible(entity.isCustomNameVisible());
            }

            zombieHorse.setPersistenceRequired();
            net.minecraftforge.event.ForgeEventFactory.onLivingConvert(entity, zombieHorse);
            entity.level.addFreshEntity(zombieHorse);
        }
    }

    @SubscribeEvent
    public static void onLevelChange(PlayerXpEvent.LevelChange event)
    {
        if (event.getEntity().level.isClientSide) return;
        if (DisableConfig.enchantingpatch.get()) return;
        String cost = getCost(event.getPlayer().getUUID());
        if (!cost.equals(""))
        {
            removeCost(event.getPlayer().getUUID());

            CTweaks.logger.info(cost);

            event.setCanceled(true);
            CTweaks.logger.info(UtilityC.getExperienceFromLevel(Math.abs(event.getLevels())));
            event.getPlayer().giveExperiencePoints(-UtilityC.getExperienceFromLevel(Math.abs(event.getLevels())));

            ((ServerPlayerEntity) event.getPlayer()).connection.send(new SSetExperiencePacket(
                    event.getPlayer().experienceProgress,
                    event.getPlayer().totalExperience,
                    event.getPlayer().experienceLevel
            ));

            setCost(event.getPlayer(), cost);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        List<MobSpawnInfo.Spawners> spawns =
                event.getSpawns().getSpawner(EntityClassification.CREATURE);

        Iterator<MobSpawnInfo.Spawners> iterator = spawns.iterator();

        MobSpawnInfo.Spawners spans = null;

        while (iterator.hasNext())
        {
            MobSpawnInfo.Spawners spawners = iterator.next();
            if (spawners.type == EntityType.FOX)
            {
                iterator.remove();
                spans = new MobSpawnInfo.Spawners(EntityInit.FOX_MODIFIED.get(), 8, 2, 4);
            }
            else if (spawners.type == EntityType.POLAR_BEAR)
            {
                iterator.remove();
                spans = new MobSpawnInfo.Spawners(EntityInit.POLAR_BEAR_MODIFIED.get(), 1, 1, 2);
            }
        }

        if (spans != null)
        {
            spawns.add(spans);
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
            cap.setTickWhenTraumatized(event.getEntity().level.getGameTime());
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
            cap.setTickWhenTraumatized(to.level.getGameTime());
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

            if (fear.startsWith("mob"))
            {
                if (event.getSource().msgId.equals("mob"))
                {
                    player.disableShield(true);
                    if (event.getAmount() < 0.1f) return;
                    player.addEffect(new EffectInstance(Effects.WEAKNESS, 15, 2));
                    cap.setFearCounter(200);
                    cap.setMaxFearCounter(200);
                }

                String mob = fear.substring(3);

            }

            if (fear.equals("arrow"))
            {
                if (event.getSource().msgId.equals("arrow"))
                {
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 2));
                    cap.setFearCounter(200);
                    cap.setMaxFearCounter(200);
                }
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
        /*if (event.getMessage().startsWith("/")) return;
        StringTextComponent component = (StringTextComponent) new StringTextComponent(event.getMessage().replaceAll("", "\\u00A7ka")).setStyle(
                Style.EMPTY.setObfuscated(true)
        );
        event.setCanceled(true);
        Minecraft.getInstance().player.se(component, Minecraft.getInstance().player.getUUID());*/
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

            if (event.player.level.getGameTime() - cap.getTickWhenTraumatized() > 40000)
            {
                if (!cap.getFear().equals(""))
                {
                    cap.setFear("");
                    if (!event.player.level.isClientSide)
                        CTweaksPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CTweaksPacketHandler.DisplayClientMessage("ctweaks.message.calm"));
                }
                return;
            }

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
