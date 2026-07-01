import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Applies the Create 6.0.10 axis-null crash fix directly to a user-supplied
 * create-1.21.1-6.0.10.jar.
 *
 * In ContinuousOBBCollider$ContinuousSeparationManifold.reset() the fields
 * {@code axis} and {@code normalAxis} are set to null; this rewrites those
 * assignments to Vec3.ZERO, matching the behaviour of the old base class and
 * preventing the NullPointerException.
 *
 * Usage: java -jar create-patcher.jar <create-1.21.1-6.0.10.jar> [output.jar]
 */
public final class Patcher {

    private static final String TARGET_CLASS =
        "com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold";
    private static final String VEC3 = "net/minecraft/world/phys/Vec3";
    private static final Set<String> FIELDS = Set.of("axis", "normalAxis");

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar create-patcher.jar <create-1.21.1-6.0.10.jar> [output.jar]");
            System.exit(1);
        }

        Path input = Paths.get(args[0]);
        if (!Files.isRegularFile(input)) {
            System.err.println("Input jar not found: " + input);
            System.exit(1);
        }

        Path output = args.length >= 2
            ? Paths.get(args[1])
            : Paths.get(input.toString().replaceFirst("(?i)\\.jar$", "") + "-fixed.jar");

        String targetEntry = TARGET_CLASS + ".class";
        boolean patched = false;

        try (ZipFile zip = new ZipFile(input.toFile());
             ZipOutputStream out = new ZipOutputStream(
                 new BufferedOutputStream(Files.newOutputStream(output)))) {

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Recreate the entry without copying compressed-size/CRC metadata.
                out.putNextEntry(new ZipEntry(entry.getName()));
                try (InputStream in = zip.getInputStream(entry)) {
                    if (entry.getName().equals(targetEntry)) {
                        byte[] original = in.readAllBytes();
                        byte[] modified = patchClass(original);
                        if (modified != null) {
                            patched = true;
                            out.write(modified);
                        } else {
                            out.write(original);
                        }
                    } else {
                        in.transferTo(out);
                    }
                }
                out.closeEntry();
            }
        }

        if (!patched) {
            System.err.println("ERROR: target method was not found/patched.");
            System.err.println("Is this really an unmodified create-1.21.1-6.0.10.jar?");
            Files.deleteIfExists(output);
            System.exit(2);
        }

        System.out.println("Done. Patched jar written to: " + output);
    }

    /** Returns rewritten bytes, or null if nothing was patched. */
    private static byte[] patchClass(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        int replaced = 0;
        for (MethodNode m : cn.methods) {
            if (!m.name.equals("reset") || !m.desc.equals("()V")) {
                continue;
            }
            for (AbstractInsnNode insn = m.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() != Opcodes.PUTFIELD) {
                    continue;
                }
                FieldInsnNode put = (FieldInsnNode) insn;
                if (!put.owner.equals(TARGET_CLASS) || !FIELDS.contains(put.name)) {
                    continue;
                }
                AbstractInsnNode prev = put.getPrevious();
                if (prev != null && prev.getOpcode() == Opcodes.ACONST_NULL) {
                    // Replace `aconst_null` with `getstatic Vec3.ZERO`.
                    m.instructions.set(prev,
                        new FieldInsnNode(Opcodes.GETSTATIC, VEC3, "ZERO", "L" + VEC3 + ";"));
                    replaced++;
                }
            }
        }

        if (replaced == 0) {
            return null;
        }

        // Stack depth is unchanged (one reference pushed then consumed), so the
        // existing stack-map frames stay valid — no COMPUTE_FRAMES needed.
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        System.out.println("Patched " + replaced + " null assignment(s) in reset().");
        return cw.toByteArray();
    }

    private Patcher() {
    }
}
