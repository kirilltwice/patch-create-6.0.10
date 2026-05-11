# Bytecode patch for ContinuousOBBCollider$ContinuousSeparationManifold.reset()
# Apply with Recaf 4.x assembler
# Target: create-1.21.1-6.0.10.jar
# Class: com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold

.method public reset ()V {
    parameters: { this },
    code: {
    A: 
        line 304
        aload this
        getstatic net/minecraft/world/phys/Vec3.ZERO Lnet/minecraft/world/phys/Vec3;
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.axis Lnet/minecraft/world/phys/Vec3;
    B: 
        line 305
        aload this
        getstatic net/minecraft/world/phys/Vec3.ZERO Lnet/minecraft/world/phys/Vec3;
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.normalAxis Lnet/minecraft/world/phys/Vec3;
    C: 
        line 306
        aload this
        ldc 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.separation D
    D: 
        line 307
        aload this
        ldc 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.stepSeparation D
    E: 
        line 308
        aload this
        ldc 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.normalSeparation D
    F: 
        line 309
        aload this
        ldc -1D
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.latestCollisionEntryTime D
    G: 
        line 310
        aload this
        ldc 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.earliestCollisionExitTime D
    H: 
        line 311
        aload this
        iconst_1 
        putfield com/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold.isDiscreteCollision Z
    I: 
        line 312
        return 
    J: 
    }
}
