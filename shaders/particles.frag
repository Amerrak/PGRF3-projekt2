in vec3 finalColor;
in vec2 textureCoord;

uniform sampler2D textureSampler;

void main(void)
{
    vec2 uv = textureCoord.xy;
   // uv.y *= -1.0;
    vec4 t = texture(textureSampler, uv);

    if(t.a < 0.5){
        discard;
    }
    gl_FragColor = vec4(t); // * finalColor
    //gl_FragColor = vec4(finalColor,1.0);
}