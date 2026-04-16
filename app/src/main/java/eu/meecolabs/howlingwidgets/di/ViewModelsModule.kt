package eu.meecolabs.howlingwidgets.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [AppModule::class]
)
@ComponentScan("eu.meecolabs.howlingwidgets.ui")
class ViewModelsModule
