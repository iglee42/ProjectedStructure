package fr.iglee42.projectedstructure.common.utils;

import com.mojang.blaze3d.platform.NativeImage;
import fr.iglee42.projectedstructure.ProjectedStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigStructures {

    private static Path STRUCTURES_PATH;
    private static Path IMAGES_PATH;

    public static void init(){
        Path configPaths = FMLPaths.CONFIGDIR.get().resolve("projectedstructure");

        STRUCTURES_PATH = configPaths.resolve("structures");
        IMAGES_PATH = configPaths.resolve("images");

        STRUCTURES_PATH.toFile().mkdirs();
        IMAGES_PATH.toFile().mkdirs();
    }


    public static List<String> getStructures(){
        return readStructureDir(STRUCTURES_PATH.toFile());
    }

    private static List<String> readStructureDir(File dir){
        List<String> structures = new ArrayList<>();
        for (String file : dir.list()) {
            File f = new File(dir, file);
            if (f.isDirectory()) structures.addAll(readStructureDir(f));
            else if (f.getName().endsWith(".nbt")) structures.add(file.substring(0,file.length() - 4));
        }
        return structures;
    }

    public static List<String> getStructuresPaths(){
        return readStructurePath(STRUCTURES_PATH.toFile());
    }
    private static List<String> readStructurePath(File dir){
        List<String> structures = new ArrayList<>();
        for (String file : dir.list()) {
            File f = new File(dir, file);
            if (f.isDirectory()) structures.addAll(readStructurePath(f));
            else if (f.getName().endsWith(".nbt")) structures.add(getParentsOfStructure(f));
        }
        return structures;
    }
    private static String getParentsOfStructure(File file){
        StringBuilder out = new StringBuilder();
        out.append(getParent(file));
        out.append("/")
           .append(file.isDirectory() ? file.getName() : file.getName().substring(0, file.getName().length() - 4));
        return out.toString();
    }

    private static String getParent(File dir){
        if (!dir.getParentFile().getAbsolutePath().equals(STRUCTURES_PATH.toAbsolutePath().toString())){
            return getParent(dir.getParentFile()) + "/" +  dir.getParentFile().getName();
        }
        return "";
    }

    public static DynamicTexture getImage(String path) {
        File iconFile = new File(IMAGES_PATH.toFile(),path + ".png");
        boolean flag = !path.isEmpty() && iconFile.exists();
        if (flag) {
            try {
                InputStream inputstream = new FileInputStream(iconFile);

                DynamicTexture dynamictexture1;
                try {
                    NativeImage nativeimage = NativeImage.read(inputstream);
                    Validate.validState(nativeimage.getWidth() == 512, "Must be 512 pixels wide");
                    Validate.validState(nativeimage.getHeight() == 512, "Must be 512 pixels high");
                    DynamicTexture dynamictexture = new DynamicTexture(nativeimage);
                    Minecraft.getInstance().getTextureManager().register(new ResourceLocation(ProjectedStructure.MODID,path), dynamictexture);
                    dynamictexture1 = dynamictexture;
                } catch (Throwable throwable1) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                    }

                    throw throwable1;
                }

                inputstream.close();
                return dynamictexture1;
            } catch (Throwable throwable2) {
                return null;
            }
        } else {
            Minecraft.getInstance().getTextureManager().release(new ResourceLocation(ProjectedStructure.MODID,path));
            return null;
        }
    }

    public static File getStructurePath(String structureName, String path) {
        return new File(STRUCTURES_PATH.toFile(),path.substring(0,path.length() - structureName.length()));
    }

    public static File getStructure(String structurePath) {
        return new File(STRUCTURES_PATH.toFile(),structurePath.substring(1) + ".nbt");
    }
}
