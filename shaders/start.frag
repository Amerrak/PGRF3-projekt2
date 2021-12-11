#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;
in vec4 depthTextureCoord;
in float dist;
in vec4 outPosition;

uniform sampler2D depthTexture;
uniform sampler2D mosaic;
uniform bool textureMode;
uniform bool ambientEnabled, diffuseEnabled, specularEnabled;
uniform float constantAttenuation, linearAttenuation, quadraticAttenuation;
uniform float spotCutOff;
uniform vec3 spotDirection;
uniform bool reflectorDisabled;
uniform int debugMode;

out vec4 outColor;

void main() {
    vec3 ambient = vec3(0.1);
    vec3 ld = normalize(light);

    float NdotL = max(0, dot(normalize(light), normalize(normal)));
    vec3 diffuse = vec3(NdotL * vec3(0.5));

    vec3 halfVector = normalize(light + viewDirection);
    float NdotH = max(0.0, dot(normalize(normal), halfVector));
    vec3 specular = vec3(pow(NdotH, 16.0));

    float att = 1.0 / (constantAttenuation + linearAttenuation * dist + quadraticAttenuation * dist * dist);
    if (!ambientEnabled) {
        ambient = vec3(0, 0, 0);
    }
    if (!diffuseEnabled) {
        diffuse = vec3(0, 0, 0);
    }
    if (!specularEnabled) {
        specular = vec3(0, 0, 0);
    }
    vec3 finalColorIntensity = ambient + att * (diffuse + specular);


    vec3 textureColor;
    if (textureMode){
        textureColor = texture(mosaic, texCoord).rgb;
    } else {
        textureColor = vec3(1, 1, 1);
    }

    // "z" hodnota z textury
    // R, G i B složky jsou stejné, protože gl_FragCoord.zzz
    // r -> v light.frag ukládáme gl_FragCoord.zzz, takže jsou všechny hodnoty stejné
    float zLight = texture(depthTexture, depthTextureCoord.xy).r;// Z hodnota ke světlu nejbližšího pixelu na této pozici

    // aktuální hodnota
    float zActual = depthTextureCoord.z;

    // 0.01 - bias na ostranění tzv. akné
    bool shadow = zActual > zLight + 0.0001;

    float spotEffect = 1;
    if (!reflectorDisabled){
        spotEffect = max(dot(normalize(spotDirection), normalize(-ld)), 0);
    }
    float blend = clamp((spotEffect - spotCutOff) / (1 - spotCutOff), 0.0, 1.0);


    if (debugMode == 0){
        if (shadow ||  spotEffect <= spotCutOff) {
            outColor = vec4(ambient * textureColor, 1.0);
        } else {
            finalColorIntensity = mix(ambient, ambient + att * (diffuse + specular), blend);
            outColor = vec4(finalColorIntensity * textureColor, 1.0);
        }
    } else if (debugMode == 1){
        outColor = outPosition;
    } else if (debugMode == 2){
        outColor = depthTextureCoord;
    } else if (debugMode == 3){
        outColor = vec4(normalize(normal), 1.0);
    } else if (debugMode == 4){
        outColor = vec4(texCoord, 0.0, 1.0);
    } else if (debugMode == 5){
        outColor = vec4(1.0 / dist, 0.0, 0.0, 1.0);
    }
}
