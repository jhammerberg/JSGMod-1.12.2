package tauri.dev.jsg.item.tools.staff;

import tauri.dev.jsg.util.main.JSGDamageSources;
import tauri.dev.jsg.entity.JSGEnergyProjectile;
import tauri.dev.jsg.entity.EntityRegister;
import tauri.dev.jsg.item.tools.EnergyWeapon;
import tauri.dev.jsg.sound.SoundEventEnum;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StaffItem extends EnergyWeapon {

    public static final String ITEM_NAME = "staff";

    public StaffItem() {
        super(ITEM_NAME, 10_000_000, 100_000);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TileEntityItemStackRenderer createTEISR() {
        return new StaffTEISR();
    }

    @Override
    public void playShootSound(World world, Entity entity){
        EntityRegister.playSoundEvent(SoundEventEnum.STAFF_SHOOT, entity);
    }

    @Override
    public int getWeaponCoolDown(){ return 30; }

    @Override
    public DamageSource getDamageSource(Entity source, Entity attacker){
        return JSGDamageSources.getDamageSourceStaff(source, attacker);
    }

    @Override
    public void setEnergyBallParams(JSGEnergyProjectile projectile) {
        super.setEnergyBallParams(projectile);
        projectile.explode = true;
        projectile.maxAliveTime = 90;
        projectile.damage = 10.0f;
        projectile.igniteGround = true;
    }
}
