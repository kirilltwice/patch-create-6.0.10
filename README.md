# Create 6.0.10 — Axis Null Fix

Fix for the `NullPointerException: Cannot read field "x" because "mf.axis" is null` crash affecting Create 6.0.10 on NeoForge 1.21.1.

## The Problem

Create 6.0.10 introduced a refactor of the collision system. In `ContinuousOBBCollider`, the inner class `ContinuousSeparationManifold.reset()` sets `this.axis` and `this.normalAxis` to `null`. When an entity's AABB center coincides exactly with a contraption block center on all SAT axes, these fields are never reassigned before being read — causing an instant server crash.

```
java.lang.NullPointerException: Cannot read field "x" because "mf.axis" is null
    at ContinuousOBBCollider.collideMany(ContinuousOBBCollider.java:153)
```

This does **not** occur in Create 6.0.9.

## Affected Mods

Any mod using Sable physics contraptions, including:
- **Create Aeronautics** (cars, planes, airships)
- **Create Offroad** (wheeled vehicles)
- **Create Simulated**

The crash can happen at any time while a vehicle is in use, not just on spawn/despawn.

## The Fix

In `ContinuousSeparationManifold.reset()`, replace the two `null` assignments with `Vec3.ZERO`:

```java
// BEFORE (buggy)
public void reset() {
    this.axis = null;
    this.normalAxis = null;
    ...
}

// AFTER (fixed)
public void reset() {
    this.axis = Vec3.ZERO;
    this.normalAxis = Vec3.ZERO;
    ...
}
```

This matches the behavior of the old `SeparationManifold` base class which initialized `axis = Vec3.ZERO`, preventing the NPE.

## How to Apply the Fix — Easy Way (patcher jar)

Each release ships a ready-to-run **`create-patcher.jar`** on the
[Releases page](../../releases). It patches your own copy of the jar for you —
no Recaf, no manual bytecode editing:

```
java -jar create-patcher.jar create-1.21.1-6.0.10.jar
```

This writes `create-1.21.1-6.0.10-fixed.jar` next to the original. Drop that
into your `mods` folder. Requires Java 17+.

> The original `create-1.21.1-6.0.10.jar` is Create's copyrighted mod and is
> **not** distributed here — the patcher only modifies the copy you already own.

## How to Apply the Fix Manually

### Requirements
- [Recaf 4.x](https://github.com/Col-E/Recaf/releases) (requires Java 22+)
- Your `create-1.21.1-6.0.10.jar`

### Steps

1. **Launch Recaf**
   ```
   java -jar recaf-4x-alpha-win-86-x64.jar
   ```

2. **Open the jar**
   File → Open → select `create-1.21.1-6.0.10.jar`

3. **Navigate to the class**
   ```
   com → simibubi → create → foundation → collision → ContinuousOBBCollider$ContinuousSeparationManifold
   ```

4. **Open the assembler**
   Right-click on the `reset()` method → Edit with assembler

5. **Apply the patch** — replace both `aconst_null` instructions:

   **Bloc A (axis):**
   ```asm
   A:
       line 304
       aload this
       getstatic net/minecraft/world/phys/Vec3.ZERO Lnet/minecraft/world/phys/Vec3;
       putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.axis Lnet/minecraft/world/phys/Vec3;
   ```

   **Bloc B (normalAxis):**
   ```asm
   B:
       line 305
       aload this
       getstatic net/minecraft/world/phys/Vec3.ZERO Lnet/minecraft/world/phys/Vec3;
       putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.normalAxis Lnet/minecraft/world/phys/Vec3;
   ```

6. **Save and export**
   File → Export → replace your original jar

## Notes

- This fix patches the jar directly — no additional mod required
- Always keep a backup of the original jar
- This is a temporary fix until Create 6.0.11 is released with an official patch
- The bug is tracked on the official Create GitHub: [Issue #10278](https://github.com/Creators-of-Create/Create/issues/10278)

## Credits

Fix discovered and applied by **Snoyxo** and friends while running a Create modded server.
Bytecode patch procedure documented with help from Claude (Anthropic).
