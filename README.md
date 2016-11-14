# Capstone

For "build.gradle" add these dependencies if you don't have them.


 dependencies {

    compile fileTree(include: ['*.jar'], dir: 'libs')
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })


    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'com.google.android.gms:play-services-maps:9.6.1'
    compile 'com.google.android.gms:play-services-location:9.6.1'
    compile 'com.google.code.gson:gson:2.7'

}
