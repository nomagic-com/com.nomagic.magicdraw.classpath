# com.nomagic.magicdraw.classpath
Classpath container plugin for eclipse


Install:

Copy /plugins/com.nomagic.magicdraw-1.0.0.jar to eclipse/plugins

Use:

1. Restart Eclipse
2. Eclipse->Preferences...->MagicDraw Installation Directory ```enter MD installation location```
3.  Eclipse->Preferences...->Run/Debug->String Substitution->New... Variable: ```md.install.dir``` Value: Click Browse/Browse To MD Install Directory (Click Ok, this step may require several attempts due to a caught eclipse error)
4.  Open Java Perspective
5.  Right click on MagicDraw project.. properties->Java Build Path->Libraries...->Add Library->MagicDraw Classpath Container
6.  Follow instructions and add the desired libraries from MD to be added to your classpath
