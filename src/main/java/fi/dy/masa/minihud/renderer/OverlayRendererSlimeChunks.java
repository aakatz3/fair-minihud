package fi.dy.masa.minihud.renderer;

import org.lwjgl.opengl.GL11;
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.config.RendererToggle;
import fi.dy.masa.minihud.util.DataStorage;
import fi.dy.masa.minihud.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;

public class OverlayRendererSlimeChunks extends OverlayRendererBase
{
    public static double overlayTopY;

    protected double topY;

    @Override
    public boolean shouldRender(Minecraft mc)
    {
        return RendererToggle.OVERLAY_SLIME_CHUNKS_OVERLAY.getBooleanValue() && mc.world.provider.isSurfaceWorld();
    }

    @Override
    public boolean needsUpdate(Entity entity, Minecraft mc)
    {
        if (this.topY != overlayTopY)
        {
            return true;
        }

        int ex = (int) Math.floor(entity.posX);
        int ez = (int) Math.floor(entity.posZ);
        int lx = this.lastUpdatePos.getX();
        int lz = this.lastUpdatePos.getZ();

        return Math.abs(lx - ex) > 16 || Math.abs(lz - ez) > 16;
    }

    @Override
    public void update(Entity entity, Minecraft mc)
    {
        DataStorage data = DataStorage.getInstance();
        this.topY = overlayTopY;

        if (data.isWorldSeedKnown(entity.dimension))
        {
            final int centerX = ((int) MathHelper.floor(entity.posX)) >> 4;
            final int centerZ = ((int) MathHelper.floor(entity.posZ)) >> 4;
            final long worldSeed = data.getWorldSeed(entity.dimension);
            final int color = Configs.Colors.SLIME_CHUNKS_OVERLAY_COLOR.getIntegerValue();
            PooledMutableBlockPos pos1 = PooledMutableBlockPos.retain();
            PooledMutableBlockPos pos2 = PooledMutableBlockPos.retain();
            int r = MathHelper.clamp(Configs.Generic.SLIME_CHUNK_OVERLAY_RADIUS.getIntegerValue(), -1, 40);

            if (r == -1)
            {
                r = mc.gameSettings.renderDistanceChunks;
            }

            BUFFER_1.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            BUFFER_2.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            for (int xOff = -r; xOff <= r; xOff++)
            {
                for (int zOff = -r; zOff <= r; zOff++)
                {
                    int cx = centerX + xOff;
                    int cz = centerZ + zOff;

                    if (MiscUtils.canSlimeSpawnInChunk(cx, cz, worldSeed))
                    {
                        pos1.setPos( cx << 4,               0,  cz << 4);
                        pos2.setPos((cx << 4) + 16, this.topY, (cz << 4) + 16);
                        RenderUtils.renderBoxWithEdgesBatched(BUFFER_1, BUFFER_2, pos1, pos2, color);
                    }
                }
            }

            pos1.release();
            pos2.release();

            BUFFER_1.finishDrawing();
            BUFFER_2.finishDrawing();

            this.renderObjects.get(0).uploadData(BUFFER_1);
            this.renderObjects.get(1).uploadData(BUFFER_2);

            this.lastUpdatePos = new BlockPos(entity);
        }
    }

    @Override
    public void allocateGlResources()
    {
        this.allocateBuffer(GL11.GL_TRIANGLE_STRIP);
        this.allocateBuffer(GL11.GL_LINE_STRIP);
    }
}