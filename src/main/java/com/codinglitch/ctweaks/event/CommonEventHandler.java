
package com.codinglitch.ctweaks.event;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.config.DisableConfig;
import com.codinglitch.ctweaks.registry.capabilities.DeathFearProvider;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import com.codinglitch.ctweaks.registry.init.EntityInit;
import com.codinglitch.ctweaks.registry.init.ItemsInit;
import com.codinglitch.ctweaks.registry.items.ScytheItem;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.UtilityC;
import com.codinglitch.ctweaks.util.network.CTweaksPacketHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;

@EventBusSubscriber
public class CommonEventHandler {
    public static Random rand = new Random();
    public static BlockPattern mobGolemFull;

    public static class EatRepairGoal extends Goal
    {
        Mob mob;
        Item food;
        SoundEvent sound;
        int ticksLasted = 0;

        Predicate<ItemEntity> ALLOWED_ITEMS;

        public EatRepairGoal(Mob mob)
        {
            this.mob = mob;
            if (mob instanceof Zombie)
            {
                food = Items.ROTTEN_FLESH;
                sound = SoundEvents.NETHER_WART_BREAK;
            }
            else if (mob instanceof Skeleton)
            {
                food = Items.BONE;
                sound = SoundEvents.SKELETON_AMBIENT;
            }
            else if (mob instanceof Creeper)
            {
                food = Items.GUNPOWDER;
                sound = SoundEvents.SAND_BREAK;
            }

            ALLOWED_ITEMS = (item) -> {
                return !item.hasPickUpDelay() & item.isAlive() & item.getItem().getItem() == food;
            };

            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void tick() {
            List<ItemEntity> list = mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                mob.getNavigation().moveTo(list.get(0), 1.2F);
                ticksLasted++;
            }
        }

        @Override
        public void start() {
            mob.addTag("Eating");
            List<ItemEntity> list = mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                mob.getNavigation().moveTo(list.get(0), 1.2F);
            }
            ticksLasted = 0;
        }

        @Override
        public void stop() {
            mob.removeTag("Eating");

            List<ItemEntity> list = mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(1D, 1D, 1D), ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                list.get(0).getItem().shrink(1);
                mob.level.playSound(null, mob, sound, SoundSource.NEUTRAL, 1, 1);
                mob.heal(1);
            }
            ticksLasted = 0;
        }

        @Override
        public boolean canUse() {
            List<ItemEntity> list = mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), ALLOWED_ITEMS);

            return !list.isEmpty() & mob.getTarget() == null & !mob.getTags().contains("Eating");
        }

        @Override
        public boolean canContinueToUse() {
            List<ItemEntity> list = mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(1D, 1D, 1D), ALLOWED_ITEMS);
            if (list.isEmpty())
                return ticksLasted < 81;
            return ticksLasted < 21;
        }
    }

    public static final Predicate<LivingEntity> IS_MONSTER = (entity) -> {
        return !entity.getTags().contains("SummonedByPlayer");
    };

    public static List<Pair<UUID, String>> costList = new ArrayList<>();

    public static void setCost(Player player, String type)
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

    public static <T extends Mob> EntityType<T> getEntityFromSkull(BlockState skull)
    {
        if (skull.is(Blocks.ZOMBIE_HEAD))
            return (EntityType<T>) EntityType.ZOMBIE;
        if (skull.is(Blocks.SKELETON_SKULL))
            return (EntityType<T>) EntityType.SKELETON;
        if (skull.is(Blocks.CREEPER_HEAD))
            return (EntityType<T>) EntityType.CREEPER;
        return null;
    }

    public static void removeAttackableGoals(GoalSelector goalSelector, List<Class<? extends LivingEntity>> goals)
    {
        Set<WrappedGoal> set = ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, goalSelector, "f_25345_");
        Iterator<WrappedGoal> iterator = set.iterator();

        List<Goal> toRemove = new ArrayList<>();

        while (iterator.hasNext())
        {
            WrappedGoal goal = iterator.next();
            if (goal.getGoal() instanceof NearestAttackableTargetGoal)
            {
                Class<? extends LivingEntity> target = ObfuscationReflectionHelper.getPrivateValue(NearestAttackableTargetGoal.class, (NearestAttackableTargetGoal)goal.getGoal(), "f_26048_");
                if (target == null) continue;
                for (Class<? extends LivingEntity> goal1 : goals) {
                    if (target.isInstance(goal1) | target == goal1) {
                        toRemove.add(goal.getGoal());
                    }
                }
            }
        }

        for (Goal goal : toRemove) {
            goalSelector.removeGoal(goal);
        }
    }

    @SubscribeEvent
    public static void onEntityJoined(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof IronGolem)
        {
            IronGolem entity = (IronGolem) event.getEntity();

            //----- Goal Modification
            removeAttackableGoals(entity.targetSelector, new ArrayList<>(Collections.singletonList(Mob.class)));
            entity.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(entity, Mob.class, 5, false, false, (check) -> {
                return check instanceof Enemy && !(check instanceof Creeper) & IS_MONSTER.test(check);
            }));
            //-----
        }
        else if (event.getEntity() instanceof SnowGolem)
        {
            SnowGolem entity = (SnowGolem) event.getEntity();

            //----- Goal Modification
            removeAttackableGoals(entity.targetSelector, new ArrayList<>(Collections.singletonList(Mob.class)));

            entity.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(entity, Mob.class, 10, true, false, (check) -> {
                return check instanceof Enemy & IS_MONSTER.test(check);
            }));
            //-----
        }
        else if (event.getEntity() instanceof Mob)
        {
            Mob entity = (Mob) event.getEntity();

            if (entity.getTags().contains("SummonedByPlayer"))
            {
                removeAttackableGoals(entity.targetSelector, new ArrayList<>(Arrays.asList(Player.class, IronGolem.class, AbstractVillager.class)));

                entity.setCanPickUpLoot(true);
                entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Monster.class, 5, true, false, IS_MONSTER));
                entity.goalSelector.addGoal(5, new EatRepairGoal(entity));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event)
    {
        if (event.getWorld().isClientSide()) return;

        BlockState blockState = event.getPlacedBlock();
        Level world = (Level) event.getWorld();
        if (blockState.getBlock() instanceof SkullBlock)
        {
            if (mobGolemFull == null) {
                mobGolemFull = BlockPatternBuilder.start()
                        .aisle(" ", "#", "#")
                        .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SOUL_SAND)))
                        .build();
            }

            BlockPattern.BlockPatternMatch patternhelper = mobGolemFull.find(event.getWorld(), event.getPos());
            if (patternhelper != null) {
                EntityType<Mob> entityType = getEntityFromSkull(blockState);

                if (entityType == null) return;

                for (int i = 0; i < mobGolemFull.getHeight(); ++i) {
                    BlockInWorld cachedblockinfo = patternhelper.getBlock(0, i, 0);
                    world.setBlock(cachedblockinfo.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    world.levelEvent(2001, cachedblockinfo.getPos(), Block.getId(cachedblockinfo.getState()));
                }

                Mob toSpawn = entityType.create(world);

                toSpawn.addTag("SummonedByPlayer");

                BlockPos blockpos1 = patternhelper.getBlock(0, 2, 0).getPos();
                toSpawn.moveTo((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.05D, (double) blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
                world.addFreshEntity(toSpawn);

                for (ServerPlayer serverplayerentity : world.getEntitiesOfClass(ServerPlayer.class, toSpawn.getBoundingBox().inflate(5.0D))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayerentity, toSpawn);
                }

                for (int l = 0; l < mobGolemFull.getHeight(); ++l) {
                    BlockInWorld cachedblockinfo3 = patternhelper.getBlock(0, l, 0);
                    world.blockUpdated(cachedblockinfo3.getPos(), Blocks.AIR);
                }
            }
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event)
    {
        Level level = event.getEntity().level;
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

        BlockEntity tileentity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;

        LootContext.Builder lootcontext = (new LootContext.Builder((ServerLevel)level))
                .withRandom(level.random)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileentity)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, event.getEntityLiving());

        List<ItemStack> drops = blockState.getDrops(lootcontext);

        if (blockState.getBlock() instanceof CropBlock) {
            CropBlock cropsblock = (CropBlock) blockState.getBlock();
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
        if (event.getContainer() instanceof EnchantmentMenu | event.getContainer() instanceof AnvilMenu)
        {
            removeCost(event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock event)
    {
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState blockState = event.getWorld().getBlockState(pos);
        BlockEntity tileEntity = event.getWorld().getBlockEntity(pos);
        if (tileEntity instanceof EnchantmentTableBlockEntity)
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
    }

    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event)
    {
        if (event.getEntity() instanceof Horse entity)
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
            ZombieHorse zombieHorse = EntityType.ZOMBIE_HORSE.create(entity.level);
            zombieHorse.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
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

            ((ServerPlayer) event.getPlayer()).connection.send(new ClientboundSetExperiencePacket(
                    event.getPlayer().experienceProgress,
                    event.getPlayer().totalExperience,
                    event.getPlayer().experienceLevel
            ));

            setCost(event.getPlayer(), cost);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        List<MobSpawnSettings.SpawnerData> spawns =
                event.getSpawns().getSpawner(MobCategory.CREATURE);

        Iterator<MobSpawnSettings.SpawnerData> iterator = spawns.iterator();

        MobSpawnSettings.SpawnerData spans = null;

        while (iterator.hasNext())
        {
            MobSpawnSettings.SpawnerData spawners = iterator.next();
            if (spawners.type == EntityType.POLAR_BEAR)
            {
                iterator.remove();
                spans = new MobSpawnSettings.SpawnerData(EntityInit.POLAR_BEAR_MODIFIED.get(), 1, 1, 2);
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

        if (event.getEntity() instanceof Player & event.getTarget() instanceof LivingEntity & event.isVanillaCritical())
        {
            Player player = (Player) event.getEntity();

            event.setDamageModifier(Math.min(event.getDamageModifier()+player.fallDistance/10, 2.2f));
        }
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (DisableConfig.trauma.get()) return;

        if (!(event.getObject() instanceof Player)) return;

        event.addCapability(new ResourceLocation(ReferenceC.MODID, "deathfear"), new DeathFearProvider());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {
        if (DisableConfig.trauma.get()) return;

        if (!(event.getEntity() instanceof Player)) return;

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
        Player from = event.getOriginal();
        Player to = event.getPlayer();

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
    public static void onLivingHurt(LivingHurtEvent event)
    {
        if (DisableConfig.trauma.get()) return;

        if (!(event.getEntityLiving() instanceof Player)) return;

        Player player = (Player) event.getEntityLiving();

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
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 15, 2));
                    cap.setFearCounter(200);
                    cap.setMaxFearCounter(200);
                }

                String mob = fear.substring(3);

            }

            if (fear.equals("arrow"))
            {
                if (event.getSource().msgId.equals("arrow"))
                {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
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

        if (!(event.getEntityLiving() instanceof Player)) return;
        Player player = (Player) event.getEntityLiving();

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

        Player player = event.player;

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
                        CTweaksPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CTweaksPacketHandler.DisplayClientMessage("ctweaks.message.calm"));
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
                        player.level.playSound(player, new BlockPos(player.position()), SoundEvents.CREEPER_PRIMED, SoundSource.AMBIENT, 1, 1);
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
                            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 1));
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2));
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
                                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 1));
                                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2));
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
                    CTweaksPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CTweaksPacketHandler.SyncFear(cap.getFear(), cap.getFearCounter(), cap.getMaxFearCounter()));
                }
            }
        }
    }
}
