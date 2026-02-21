package com.example.axiom.ui.components.shared


import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush

// Data class to mirror your IMeshGradientColor
data class MeshGradientColor(
    val r: Float,
    val g: Float,
    val b: Float,
    val a: Float = 1f
)

// A standard AGSL Mesh Gradient Shader (replaces your Skia SkSL shader)
@org.intellij.lang.annotations.Language("AGSL")
const val MESH_GRADIENT_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float noise;
    uniform float blur;
    uniform float contrast;
    uniform half4 color1;
    uniform half4 color2;
    uniform half4 color3;
    uniform half4 color4;

    // Simple noise generator
    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }

    half4 main(in float2 fragCoord) {
        float2 uv = fragCoord / resolution.xy;
        
        // Fluid animation coordinates using sine waves and time
        float2 p1 = float2(sin(time * 0.5) * 0.5 + 0.5, cos(time * 0.3) * 0.5 + 0.5);
        float2 p2 = float2(cos(time * 0.4) * 0.5 + 0.5, sin(time * 0.6) * 0.5 + 0.5);
        float2 p3 = float2(sin(time * 0.7) * 0.5 + 0.5, cos(time * 0.2) * 0.5 + 0.5);
        float2 p4 = float2(cos(time * 0.5) * 0.5 + 0.5, sin(time * 0.8) * 0.5 + 0.5);

        // Distance fields calculation
        float d1 = distance(uv, p1);
        float d2 = distance(uv, p2);
        float d3 = distance(uv, p3);
        float d4 = distance(uv, p4);

        // Calculate weights (the +0.001 prevents division by zero)
        float w1 = 1.0 / (d1 + 0.001);
        float w2 = 1.0 / (d2 + 0.001);
        float w3 = 1.0 / (d3 + 0.001);
        float w4 = 1.0 / (d4 + 0.001);
        float sum = w1 + w2 + w3 + w4;

        // Blend colors based on distance weights and apply contrast
        half4 finalColor = (color1 * w1 + color2 * w2 + color3 * w3 + color4 * w4) / sum;
        finalColor.rgb = pow(finalColor.rgb, half3(contrast));
        
        // Apply grain/noise
        float n = random(uv + time) * noise * 0.1;
        finalColor.rgb += n;

        return finalColor;
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AnimatedMeshGradient(
    colors: List<MeshGradientColor>,
    modifier: Modifier = Modifier,
    speed: Float = 1f,
    noise: Float = 0.15f,
    blur: Float = 0.4f,
    contrast: Float = 1f,
    animated: Boolean = true
) {
    var time by remember { mutableFloatStateOf(0f) }

    // Replaces useFrameCallback to animate the time uniform smoothly
    LaunchedEffect(animated, speed) {
        if (animated) {
            var lastFrameTime = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameTime ->
                    // Convert nanoseconds to seconds for smooth time scaling
                    val deltaSeconds = (frameTime - lastFrameTime) / 1_000_000_000f
                    time += deltaSeconds * speed
                    lastFrameTime = frameTime
                }
            }
        }
    }

    // Ensure we always have exactly 4 colors to pass to the shader
    val safeColors = remember(colors) {
        val list = colors.toMutableList()
        while (list.size < 4) {
            list.add(list.lastOrNull() ?: MeshGradientColor(0f, 0f, 0f))
        }
        list.take(4)
    }

    val shader = remember { RuntimeShader(MESH_GRADIENT_SHADER) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Feed uniforms into the AGSL shader
        shader.setFloatUniform("resolution", size.width, size.height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("noise", noise.coerceIn(0f, 1f))
        shader.setFloatUniform("blur", blur.coerceIn(0f, 1f))
        shader.setFloatUniform("contrast", contrast.coerceIn(0f, 2f))

        shader.setFloatUniform(
            "color1",
            safeColors[0].r,
            safeColors[0].g,
            safeColors[0].b,
            safeColors[0].a
        )
        shader.setFloatUniform(
            "color2",
            safeColors[1].r,
            safeColors[1].g,
            safeColors[1].b,
            safeColors[1].a
        )
        shader.setFloatUniform(
            "color3",
            safeColors[2].r,
            safeColors[2].g,
            safeColors[2].b,
            safeColors[2].a
        )
        shader.setFloatUniform(
            "color4",
            safeColors[3].r,
            safeColors[3].g,
            safeColors[3].b,
            safeColors[3].a
        )

        drawRect(brush = ShaderBrush(shader))
    }
}