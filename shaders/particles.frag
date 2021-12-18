in vec3 finalColor;
in vec2 textureCoord;
in float lifeLength;

uniform float time;
uniform float cycleTime;
uniform int rowCount;
uniform int columnCount;
uniform int cubeMode;

uniform sampler2D textureSampler;

void main(void)
{
    if(cubeMode == 1){
        gl_FragColor = vec4(finalColor,1.0);
    }else {
        // Počet textur v atlasu
        float stageCount = rowCount*columnCount;

        // Převedení času na interval <0,1>
        float calculatedTime = lifeLength/cycleTime;

        // Výpočet aktuální pozice v atlasu
        float atlasProgression = calculatedTime * stageCount;
        int index = floor(atlasProgression);
        int column = mod(index, columnCount);
        int row = index / rowCount;

        // Výpočet offsetu pro texturu
        vec2 offset = vec2(float(column) / columnCount, float(row) / rowCount);
        vec2 textureCoordWithOffset = textureCoord / vec2(columnCount,rowCount) + offset;

        vec2 uv = textureCoordWithOffset.xy;
        vec4 t = texture(textureSampler, uv);

        if(t.a < 0.1){
            discard;
        }
        gl_FragColor = vec4(t);
    }
}