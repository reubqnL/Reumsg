A lightweight welcome message plugin for Paper servers. I built this because I wanted a way to edit join messages in-game without having to dig through config files every five seconds lol

Features
  MiniMessage Support: Use modern tags like <gradient> or <rainbow> instead of old-school color codes.

  Live Editing: Change the join text, bolding, and italics directly from the chat.

  Tab Completion: Don't remember the command? Just hit TAB and it'll walk you through the options.

  PlaceholderAPI: Fully compatible with PAPI if you have it installed.

Commands
  /reumsg id=<id> <msg> — Set the raw text for a message.

  /reumsg id=<id> bold <true/false> — Toggle the bold tag.

  /reumsg id=<id> italic <true/false> — Toggle the italic tag.

  /reumsg reload — Refresh the config if you made manual changes.

  Tip: By default, ID 1 is used for the player join message!

Installation
  Drop the Reumsg-1.1.jar into your /plugins/ folder.
  Restart the server.
  Use /reumsg to start customizing.
