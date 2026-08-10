sed -i 's/fallbackToDestructiveMigration()/fallbackToDestructiveMigration(dropAllTables = true)/g' app/src/main/java/com/example/data/database/AppDatabase.kt
sed -i 's/Icons.Default.OpenInNew/Icons.AutoMirrored.Filled.OpenInNew/g' app/src/main/java/com/example/ui/components/ApiKeyDialog.kt
sed -i 's/Icons.Default.HelpOutline/Icons.AutoMirrored.Filled.HelpOutline/g' app/src/main/java/com/example/ui/components/SupportDialog.kt
sed -i 's/Icons.Default.Send/Icons.AutoMirrored.Filled.Send/g' app/src/main/java/com/example/ui/components/SupportDialog.kt
sed -i 's/Icons.Default.HelpOutline/Icons.AutoMirrored.Filled.HelpOutline/g' app/src/main/java/com/example/ui/home/HomeScreen.kt
sed -i 's/Icons.Default.HelpOutline/Icons.AutoMirrored.Filled.HelpOutline/g' app/src/main/java/com/example/ui/profile/ProfileScreen.kt
