in vec3 finalColor;
in float lifeLength;

uniform float time;
uniform float cycleTime;




uniform sampler2D particleStar;

void main(void)
{
    float stageCount = 8.0*8.0;

    float calculatedTime = lifeLength/cycleTime; // t od 0 do 1
    float atlasProgression = calculatedTime * stageCount; // od 0 do 16
    int index = floor(atlasProgression); // index od 0 do 16
    int column = mod(index, 8);
    int row = index / 8;

    vec2 offset = vec2(float(column) / 8, float(row) / 8);

    vec2 textureCoordWithOffset = gl_PointCoord / vec2(8,8) + offset;

    // textureCoord <0;1>
    // column <0;4>
    // row <0;4>
    // offset <0;1>


    vec2 uv = textureCoordWithOffset.xy;
   // uv.y *= -1.0;



    vec4 t = texture(particleStar, uv);

    if(t.a < 0.1){
        discard;
    }
    gl_FragColor = vec4(t); // * finalColor



    //gl_FragColor = vec4(finalColor,1.0);
}