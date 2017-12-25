package club.nsdn.nyasamaoptics.Util;

import cn.ac.nya.nsasm.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.9.20.
 */
public abstract class NSASM extends cn.ac.nya.nsasm.NSASM {

    public static String[][] getCode(NBTTagList list) {
        String codeBuf = "";
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++)
                codeBuf = codeBuf.concat(list.getStringTagAt(i) + "\n");
        } else {
            codeBuf = "prt \"Code is Empty!\"\n";
        }

        return Util.getSegments(codeBuf);
    }

    public static String getCodeString(NBTTagList list) {
        String codeBuf = "";
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++)
                codeBuf = codeBuf.concat(list.getStringTagAt(i) + "\n");
        } else {
            codeBuf = "prt \"Code is Empty!\"\n";
        }

        return codeBuf;
    }

    public NSASM(String[][] code) {
        super(64, 32, 32, code);
    }

    public NSASM(String code) {
        super(64, 32, 32, Util.getSegments(code));
    }

    private void print(String value) {
        if (getPlayer() == null) return;
        getPlayer().addChatComponentMessage(new ChatComponentText(value));
    }

    @Override
    protected void loadFuncList() {
        super.loadFuncList();

        funcList.replace("prt", (dst, src) -> {
            if (src != null) {
                if (dst.type == RegType.STR) {
                    if (dst.readOnly) return Result.ERR;
                    if (src.type == RegType.CHAR && src.data.equals('\b')) {
                        if (dst.data.toString().contains("\n")) {
                            String[] parts = dst.data.toString().split("\n");
                            String res = "";
                            for (int i = 0; i < parts.length - 1; i++) {
                                res = res.concat(parts[i]);
                                if (i < parts.length - 2) res = res.concat("\n");
                            }
                        }
                    } else if (src.type == RegType.CODE) {
                        Register register = eval(src);
                        if (register == null) return Result.ERR;
                        dst.data = dst.data.toString().concat('\n' + register.data.toString());
                    } else if (src.type == RegType.STR) {
                        dst.data = dst.data.toString().concat('\n' + src.data.toString());
                    } else return Result.ERR;
                } else if (dst.type == RegType.CODE) {
                    if (dst.readOnly) return Result.ERR;
                    if (src.type == RegType.CHAR && src.data.equals('\b')) {
                        if (dst.data.toString().contains("\n")) {
                            String[] parts = dst.data.toString().split("\n");
                            String res = "";
                            for (int i = 0; i < parts.length - 1; i++) {
                                res = res.concat(parts[i]);
                                if (i < parts.length - 2) res = res.concat("\n");
                            }
                        }
                    } else if (src.type == RegType.CODE) {
                        dst.data = dst.data.toString().concat('\n' + src.data.toString());
                    } else if (src.type == RegType.STR) {
                        dst.data = dst.data.toString().concat('\n' + src.data.toString());
                    } else return Result.ERR;
                } else return Result.ERR;
            } else {
                if (dst == null) return Result.ERR;
                if (dst.type == RegType.STR) {
                    print(((String) dst.data).substring(dst.strPtr));
                } else if (dst.type == RegType.CODE) {
                    Register register = eval(dst);
                    if (register == null) return Result.ERR;
                    print(register.data.toString());
                } else print(dst.data.toString());
            }
            return Result.OK;
        });

        loadFunc(funcList);
    }

    public abstract World getWorld();
    public abstract double getX();
    public abstract double getY();
    public abstract double getZ();
    public abstract EntityPlayer getPlayer();

    public abstract void loadFunc(LinkedHashMap<String, Operator> funcList);

}
