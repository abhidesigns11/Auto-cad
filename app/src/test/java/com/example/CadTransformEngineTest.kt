package com.example

import com.example.model.CadEntity
import com.example.model.CadPoint2D
import com.example.model.CadTransformEngine
import com.example.model.IsometricEngine
import com.example.model.StandardSteelDatabase
import com.example.model.Vector3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CadTransformEngineTest {

    @Test
    fun testScreenToWorldAndBack() {
        val panX = 100f
        val panY = 200f
        val zoom = 0.5f

        val worldPt = CadPoint2D(500f, 300f)
        val screenPt = CadTransformEngine.worldToScreen(worldPt, panX, panY, zoom)

        assertEquals(350f, screenPt.x, 0.01f)
        assertEquals(350f, screenPt.y, 0.01f)

        val recoveredWorld = CadTransformEngine.screenToWorld(screenPt.x, screenPt.y, panX, panY, zoom)
        assertEquals(worldPt.x, recoveredWorld.x, 0.01f)
        assertEquals(worldPt.y, recoveredWorld.y, 0.01f)
    }

    @Test
    fun testSnapToGrid() {
        val raw = CadPoint2D(104f, 196f)
        val snapped = CadTransformEngine.snapToGrid(raw, 100f, true)
        assertEquals(100f, snapped.x, 0.01f)
        assertEquals(200f, snapped.y, 0.01f)
    }

    @Test
    fun testOrthoMode() {
        val start = CadPoint2D(0f, 0f)
        val end = CadPoint2D(250f, 40f) // dx > dy -> should lock horizontal
        val orthoPt = CadTransformEngine.applyOrtho(start, end, true)
        assertEquals(250f, orthoPt.x, 0.01f)
        assertEquals(0f, orthoPt.y, 0.01f)
    }

    @Test
    fun testSteelProfilesDatabase() {
        assertTrue(StandardSteelDatabase.W_PROFILES.isNotEmpty())
        val w310 = StandardSteelDatabase.W_PROFILES.find { it.name.startsWith("W310") }
        assertNotNull(w310)
        assertTrue(w310!!.weightPerMeter > 0f)
    }

    @Test
    fun test3DIsometricProjection() {
        val v = Vector3(1000f, 2000f, 1000f)
        val proj = IsometricEngine.projectPoint(
            point = v,
            pitchDeg = 30f,
            yawDeg = 45f,
            rollDeg = 0f,
            viewScale = 0.1f,
            viewportCenterX = 400f,
            viewportCenterY = 400f
        )
        assertTrue(proj.first > 0f)
        assertTrue(proj.second > 0f)
    }
}
