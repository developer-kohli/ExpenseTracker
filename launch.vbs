Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = "D:\ExpenseTracker"
WshShell.Run "javaw -cp ""out;lib\mysql-connector-j-8.3.0.jar"" Main", 0, False
