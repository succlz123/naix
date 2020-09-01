package org.succlz123.naix.plugin;

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.org.objectweb.asm.AnnotationVisitor;
import org.jetbrains.org.objectweb.asm.Label;
import org.jetbrains.org.objectweb.asm.MethodVisitor;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.Type;
import org.jetbrains.org.objectweb.asm.TypePath;

public class CopyLocalVariablesSorter extends MethodVisitor {
    static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");
    static final Type STRING_TYPE = Type.getObjectType("java/lang/String");
    private int[] remappedVariableIndices;
    private Object[] remappedLocalTypes;
    protected final int firstLocal;
    protected int nextLocal;
    private LocalMessageCollector messageCollector;

    public CopyLocalVariablesSorter(LocalMessageCollector messageCollector, int access, String descriptor, MethodVisitor methodVisitor) {
        this(messageCollector, 458752, access, descriptor, methodVisitor);
    }

    protected CopyLocalVariablesSorter(LocalMessageCollector messageCollector, int api, int access, String descriptor, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
        this.messageCollector = messageCollector;
        this.remappedVariableIndices = new int[40];
        this.remappedLocalTypes = new Object[20];
        this.nextLocal = (8 & access) == 0 ? 1 : 0;
        Type[] var5 = Type.getArgumentTypes(descriptor);
        int var6 = var5.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            Type argumentType = var5[var7];
            this.nextLocal += argumentType.getSize();
        }

        this.firstLocal = this.nextLocal;
    }

    public void visitVarInsn(int opcode, int var) {
        Type varType;
        switch (opcode) {
            case 21:
            case 54:
                varType = Type.INT_TYPE;
                break;
            case 22:
            case 55:
                varType = Type.LONG_TYPE;
                break;
            case 23:
            case 56:
                varType = Type.FLOAT_TYPE;
                break;
            case 24:
            case 57:
                varType = Type.DOUBLE_TYPE;
                break;
            case 25:
            case 58:
            case 169:
                varType = OBJECT_TYPE;
                break;
            default:
                throw new IllegalArgumentException("Invalid opcode " + opcode);
        }

        int index = this.remap(var, varType);
        super.visitVarInsn(opcode, index);
        messageCollector.log(
                NaixClassBuilder.TAG + " visitVarInsn -> index: " + var + ", new index: " + index
                        + ", type: " + varType
        );
    }

    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(this.remap(var, Type.INT_TYPE), increment);
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, this.nextLocal);
    }

    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        int remappedIndex = this.remap(index, Type.getType(descriptor));
        super.visitLocalVariable(name, descriptor, signature, start, end, remappedIndex);
        messageCollector.log(
                NaixClassBuilder.TAG + " visitLocalVariable -> name: " + name + ", index: " + index
                        + ", remappedIndex: " + remappedIndex + ", descriptor: " + descriptor
        );
    }

    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        Type type = Type.getType(descriptor);
        int[] remappedIndex = new int[index.length];

        for (int i = 0; i < remappedIndex.length; ++i) {
            remappedIndex[i] = this.remap(index[i], type);
        }

        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, remappedIndex, descriptor, visible);
    }

    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        if (type != -1) {
            throw new IllegalArgumentException("LocalVariablesSorter only accepts expanded frames (see ClassReader.EXPAND_FRAMES)");
        } else {
            Object[] oldRemappedLocals = new Object[this.remappedLocalTypes.length];
            System.arraycopy(this.remappedLocalTypes, 0, oldRemappedLocals, 0, oldRemappedLocals.length);
            this.updateNewLocals(this.remappedLocalTypes);
            int oldVar = 0;

            int newVar;
            for (newVar = 0; newVar < numLocal; ++newVar) {
                Object localType = local[newVar];
                if (localType != Opcodes.TOP) {
                    Type varType = OBJECT_TYPE;
                    if (localType == Opcodes.INTEGER) {
                        varType = Type.INT_TYPE;
                    } else if (localType == Opcodes.FLOAT) {
                        varType = Type.FLOAT_TYPE;
                    } else if (localType == Opcodes.LONG) {
                        varType = Type.LONG_TYPE;
                    } else if (localType == Opcodes.DOUBLE) {
                        varType = Type.DOUBLE_TYPE;
                    } else if (localType instanceof String) {
                        varType = Type.getObjectType((String) localType);
                    }

                    this.setFrameLocal(this.remap(oldVar, varType), localType);
                }

                oldVar += localType != Opcodes.LONG && localType != Opcodes.DOUBLE ? 1 : 2;
            }

            oldVar = 0;
            newVar = 0;
            int remappedNumLocal = 0;

            while (true) {
                while (oldVar < this.remappedLocalTypes.length) {
                    Object localType = this.remappedLocalTypes[oldVar];
                    oldVar += localType != Opcodes.LONG && localType != Opcodes.DOUBLE ? 1 : 2;
                    if (localType != null && localType != Opcodes.TOP) {
                        this.remappedLocalTypes[newVar++] = localType;
                        remappedNumLocal = newVar;
                    } else {
                        this.remappedLocalTypes[newVar++] = Opcodes.TOP;
                    }
                }

                super.visitFrame(type, remappedNumLocal, this.remappedLocalTypes, numStack, stack);
                this.remappedLocalTypes = oldRemappedLocals;
                return;
            }
        }
    }

    public int newLocal(Type type) {
        Object localType;
        switch (type.getSort()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                localType = Opcodes.INTEGER;
                break;
            case 6:
                localType = Opcodes.FLOAT;
                break;
            case 7:
                localType = Opcodes.LONG;
                break;
            case 8:
                localType = Opcodes.DOUBLE;
                break;
            case 9:
                localType = type.getDescriptor();
                break;
            case 10:
                localType = type.getInternalName();
                break;
            default:
                throw new AssertionError();
        }

        int local = this.newLocalMapping(type);
        this.setLocalType(local, type);
        this.setFrameLocal(local, localType);
        return local;
    }

    protected void updateNewLocals(Object[] newLocals) {
    }

    protected void setLocalType(int local, Type type) {
    }

    private void setFrameLocal(int local, Object type) {
        int numLocals = this.remappedLocalTypes.length;
        if (local >= numLocals) {
            Object[] newRemappedLocalTypes = new Object[Math.max(2 * numLocals, local + 1)];
            System.arraycopy(this.remappedLocalTypes, 0, newRemappedLocalTypes, 0, numLocals);
            this.remappedLocalTypes = newRemappedLocalTypes;
        }

        this.remappedLocalTypes[local] = type;
    }

    private int remap(int var, Type type) {
        if (var + type.getSize() <= this.firstLocal) {
            return var;
        } else {
            int key = 2 * var + type.getSize() - 1;
            int size = this.remappedVariableIndices.length;
            if (key >= size) {
                int[] newRemappedVariableIndices = new int[Math.max(2 * size, key + 1)];
                System.arraycopy(this.remappedVariableIndices, 0, newRemappedVariableIndices, 0, size);
                this.remappedVariableIndices = newRemappedVariableIndices;
            }

            int value = this.remappedVariableIndices[key];
            if (value == 0) {
                value = this.newLocalMapping(type);
                this.setLocalType(value, type);
                this.remappedVariableIndices[key] = value + 1;
            } else {
                --value;
            }

            return value;
        }
    }

    protected int newLocalMapping(Type type) {
        int local = this.nextLocal;
        this.nextLocal += type.getSize();
        return local;
    }
}