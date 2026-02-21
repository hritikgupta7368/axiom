package com.example.axiom.ui.components.shared


import android.graphics.Color.parseColor
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

// The exact AGSL translation of your Skia AURORA_VERTEX_SHADER
@org.intellij.lang.annotations.Language("AGSL")
const val AURORA_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float3 color1;
    uniform float3 color2;
    uniform float3 color3;
    uniform float3 skyTop;
    uniform float3 skyBottom;
    uniform float speed;
    uniform float intensity;
    uniform float2 waveDirection;

    float hash(float n) {
        return fract(sin(n) * 43758.5453);
    }

    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float a = 3.0;
        float2 u = f * f * (a - 2.0 * f);
        return mix(
            mix(hash(i.x + hash(i.y)), hash(i.x + 1.0 + hash(i.y)), u.x),
            // Note: AGSL requires explicit floats, so 1 -> 1.0
            mix(hash(i.x + hash(i.y + 1.0)), hash(i.x + 1.0 + hash(i.y + 1.0)), u.x),
            u.y
        );
    }

    float3 auroraLayer(float2 uv, float layerSpeed, float layerIntensity, float3 color) {
        float t = time * layerSpeed * speed;
        float2 p = uv * 2.0 + t * waveDirection;
        float n = noise(p + noise(color.xy + p + t));
        float aurora = (n - uv.y * 0.5);
        return color * aurora * layerIntensity * intensity * 2.0;
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        uv.x *= resolution.x / resolution.y;
        float3 color = float3(0.0);
        
        color += auroraLayer(uv, 0.05, 0.3, color1);
        color += auroraLayer(uv, 0.1, 0.4, color2);
        color += auroraLayer(uv, 0.15, 0.2, color3);
        color += auroraLayer(uv, 0.25, 0.3, color1 * 0.5 + color3 * 0.2);
        
        color += skyTop * (1.0 - smoothstep(0.4, 1.0, uv.y));
        color += skyBottom * (1.0 - smoothstep(0.5, 0.9, uv.y));
        
        return half4(color.r, color.g, color.b, 1.0);
    }
"""

// Helper to convert hex strings to RGB FloatArrays for the shader
private fun hexToFloat3(hex: String): FloatArray {
    val parsed = try {
        parseColor(hex)
    } catch (e: Exception) {
        parseColor("#000000")
    }
    return floatArrayOf(
        android.graphics.Color.red(parsed) / 255f,
        android.graphics.Color.green(parsed) / 255f,
        android.graphics.Color.blue(parsed) / 255f
    )
}

@Composable
fun Aurora(
    modifier: Modifier = Modifier,
    auroraColors: List<String> = listOf("#00FF87", "#60EFFF", "#B967FF"),
    skyColors: List<String> = listOf("#020308", "#0D1B2A"),
    speed: Float = 0.5f,
    intensity: Float = 1f,
    waveDirection: Pair<Float, Float> = Pair(9f, -9f)
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AuroraShaderLayer(
            modifier = modifier,
            auroraColors = auroraColors,
            skyColors = skyColors,
            speed = speed,
            intensity = intensity,
            waveDirection = waveDirection
        )
    } else {
        // Fallback for Android 12 and below
        Box(
            modifier = modifier.background(
                Color(parseColor(skyColors.firstOrNull() ?: "#020308"))
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AuroraShaderLayer(
    modifier: Modifier,
    auroraColors: List<String>,
    skyColors: List<String>,
    speed: Float,
    intensity: Float,
    waveDirection: Pair<Float, Float>
) {
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastFrameTime = withFrameNanos { it }
        while (true) {
            withFrameNanos { frameTime ->
                time += (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
            }
        }
    }

    val shader = remember { RuntimeShader(AURORA_SHADER) }

    // Parse colors exactly once when they change
    val c1 = remember(auroraColors) { hexToFloat3(auroraColors.getOrNull(0) ?: "#00FF87") }
    val c2 = remember(auroraColors) { hexToFloat3(auroraColors.getOrNull(1) ?: "#60EFFF") }
    val c3 = remember(auroraColors) { hexToFloat3(auroraColors.getOrNull(2) ?: "#B967FF") }
    val sTop = remember(skyColors) { hexToFloat3(skyColors.getOrNull(0) ?: "#020308") }
    val sBot = remember(skyColors) { hexToFloat3(skyColors.getOrNull(1) ?: "#0D1B2A") }

    Canvas(modifier = modifier) {
        shader.setFloatUniform("resolution", size.width, size.height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("speed", speed)
        shader.setFloatUniform("intensity", intensity)
        shader.setFloatUniform("waveDirection", waveDirection.first, waveDirection.second)

        shader.setFloatUniform("color1", c1[0], c1[1], c1[2])
        shader.setFloatUniform("color2", c2[0], c2[1], c2[2])
        shader.setFloatUniform("color3", c3[0], c3[1], c3[2])
        shader.setFloatUniform("skyTop", sTop[0], sTop[1], sTop[2])
        shader.setFloatUniform("skyBottom", sBot[0], sBot[1], sBot[2])

        drawRect(brush = ShaderBrush(shader))
    }
}