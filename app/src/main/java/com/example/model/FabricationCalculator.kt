package com.example.model

import kotlin.math.*

data class ConeFlatPatternResult(
    val topDiaMm: Float,
    val bottomDiaMm: Float,
    val heightMm: Float,
    val thicknessMm: Float,
    val slantHeightMm: Float,
    val innerRadiusR1: Float,
    val outerRadiusR2: Float,
    val sectorAngleDeg: Float,
    val blankChordLengthMm: Float,
    val blankPlateWidthMm: Float,
    val blankPlateHeightMm: Float,
    val weightKg: Float
)

data class ShellRollPatternResult(
    val insideDiaMm: Float,
    val shellHeightMm: Float,
    val thicknessMm: Float,
    val meanDiaMm: Float,
    val blankCutLengthMm: Float,
    val blankCutWidthMm: Float,
    val blankAreaM2: Float,
    val weightKg: Float
)

data class DishedHeadBlankResult(
    val outsideDiaMm: Float,
    val headType: DishedHeadType,
    val thicknessMm: Float,
    val straightFlangeMm: Float,
    val estimatedBlankDiaMm: Float,
    val blankWeightKg: Float,
    val internalVolumeLiters: Float
)

object FabricationCalculator {

    // SS 304 density in kg/mm³ is 7.93e-6
    private const val SS304_DENSITY_KG_MM3 = 7.93e-6f

    fun calculateConeFlatPattern(
        topDiaMm: Float,
        bottomDiaMm: Float,
        heightMm: Float,
        thicknessMm: Float = 5f
    ): ConeFlatPatternResult {
        val d1 = maxOf(topDiaMm, bottomDiaMm)
        val d2 = minOf(topDiaMm, bottomDiaMm)
        val deltaR = (d1 - d2) * 0.5f
        val slantHeight = sqrt(heightMm * heightMm + deltaR * deltaR)

        val innerRadius = if (deltaR > 0.001f) (d2 * 0.5f * slantHeight) / deltaR else 0f
        val outerRadius = innerRadius + slantHeight
        val sectorAngle = if (outerRadius > 0f) (d1 * PI.toFloat() / (2f * PI.toFloat() * outerRadius)) * 360f else 360f

        val angleRad = Math.toRadians(sectorAngle.toDouble())
        val chord = (2f * outerRadius * sin(angleRad / 2.0)).toFloat()

        val plateW = if (sectorAngle <= 180f) outerRadius * 2f else outerRadius * 2f
        val plateH = outerRadius

        val devAreaMm2 = (sectorAngle / 360f) * PI.toFloat() * (outerRadius * outerRadius - innerRadius * innerRadius)
        val weight = devAreaMm2 * thicknessMm * SS304_DENSITY_KG_MM3

        return ConeFlatPatternResult(
            topDiaMm = topDiaMm,
            bottomDiaMm = bottomDiaMm,
            heightMm = heightMm,
            thicknessMm = thicknessMm,
            slantHeightMm = slantHeight,
            innerRadiusR1 = innerRadius,
            outerRadiusR2 = outerRadius,
            sectorAngleDeg = sectorAngle,
            blankChordLengthMm = chord,
            blankPlateWidthMm = plateW,
            blankPlateHeightMm = plateH,
            weightKg = weight
        )
    }

    fun calculateShellRollPattern(
        insideDiaMm: Float,
        shellHeightMm: Float,
        thicknessMm: Float = 5f
    ): ShellRollPatternResult {
        val meanDia = insideDiaMm + thicknessMm
        val blankCutLength = (PI * meanDia).toFloat()
        val blankCutWidth = shellHeightMm
        val areaM2 = (blankCutLength * blankCutWidth) / 1_000_000f
        val weight = blankCutLength * blankCutWidth * thicknessMm * SS304_DENSITY_KG_MM3

        return ShellRollPatternResult(
            insideDiaMm = insideDiaMm,
            shellHeightMm = shellHeightMm,
            thicknessMm = thicknessMm,
            meanDiaMm = meanDia,
            blankCutLengthMm = blankCutLength,
            blankCutWidthMm = blankCutWidth,
            blankAreaM2 = areaM2,
            weightKg = weight
        )
    }

    fun calculateDishedHeadBlank(
        outsideDiaMm: Float,
        headType: DishedHeadType,
        thicknessMm: Float = 5f,
        straightFlangeMm: Float = 40f
    ): DishedHeadBlankResult {
        val factor = when (headType) {
            DishedHeadType.ELLIPSOIDAL_2_TO_1 -> 1.14f
            DishedHeadType.TORISPHERICAL -> 1.11f
            DishedHeadType.HEMISPHERICAL -> 1.57f
            DishedHeadType.FLAT_FLANGED -> 1.0f
        }
        val blankDia = outsideDiaMm * factor + 2f * straightFlangeMm
        val blankRadius = blankDia * 0.5f
        val blankAreaMm2 = PI.toFloat() * blankRadius * blankRadius
        val weight = blankAreaMm2 * thicknessMm * SS304_DENSITY_KG_MM3

        val volumeFactor = when (headType) {
            DishedHeadType.ELLIPSOIDAL_2_TO_1 -> (PI / 24.0) * (outsideDiaMm.toDouble().pow(3)) * 1e-6
            DishedHeadType.TORISPHERICAL -> (PI / 27.0) * (outsideDiaMm.toDouble().pow(3)) * 1e-6
            DishedHeadType.HEMISPHERICAL -> (2.0 * PI / 3.0) * ((outsideDiaMm * 0.5).toDouble().pow(3)) * 1e-6
            DishedHeadType.FLAT_FLANGED -> 0.0
        }

        return DishedHeadBlankResult(
            outsideDiaMm = outsideDiaMm,
            headType = headType,
            thicknessMm = thicknessMm,
            straightFlangeMm = straightFlangeMm,
            estimatedBlankDiaMm = blankDia,
            blankWeightKg = weight,
            internalVolumeLiters = volumeFactor.toFloat()
        )
    }
}
