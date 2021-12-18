in vec3 finalColor;
in float lifeLength;

uniform float time;
uniform float cycleTime;
uniform sampler2D textureSampler;
uniform int rowCount;
uniform int columnCount;

void main(void)
{
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
    vec2 textureCoordWithOffset = gl_PointCoord / vec2(columnCount,rowCount) + offset;

    vec2 uv = textureCoordWithOffset.xy;
    vec4 t = texture(textureSampler, uv);

    if(t.a < 0.1){
        discard;
    }
    gl_FragColor = vec4(t);
}



