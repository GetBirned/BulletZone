-- BEGIN NOT ATOMIC
    -- Common table -- lists global unrelated constants in a single row
    CREATE TABLE IF NOT EXISTS Common
    (
      Version VARCHAR(40) NOT NULL
    );

    -- Status table -- enumeration of statuses for items/accounts
    --                 that can be made inactive or deleted
    CREATE TABLE IF NOT EXISTS Status
    (
      StatusID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      PRIMARY KEY (StatusID)
    );

    -- EntityType table -- enumeration of basic types of things in the database
    CREATE TABLE IF NOT EXISTS EntityType
    (
      EntityTypeID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      PRIMARY KEY (EntityTypeID)
    );

    -- Entity table -- Core information about various things in the database that
    --                 are involved in ownership and have statuses and created/deleted dates
    CREATE TABLE IF NOT EXISTS Entity
    (
      EntityID INT NOT NULL AUTO_INCREMENT,
      EntityTypeID INT NOT NULL,
      StatusID INT NOT NULL,
      Created DATETIME,
      Deleted DATETIME,
      PRIMARY KEY (EntityID),
      FOREIGN KEY (EntityTypeID) REFERENCES EntityType(EntityTypeID),
      FOREIGN KEY (StatusID) REFERENCES Status(StatusID)
    );

    -- User table -- basic information for user accounts
    CREATE TABLE IF NOT EXISTS User
    (
      EntityID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      Username VARCHAR(40) NOT NULL,
      PasswordHash VARCHAR(32) NOT NULL,
      PasswordSalt VARCHAR(32) NOT NULL,
      PRIMARY KEY (EntityID),
      FOREIGN KEY (EntityID) REFERENCES Entity(EntityID)
    );

    -- UserAssociation table -- correlation between a user and another entity and/or value
    --                          and/or string. The Tag field gives a way to assign a "type" to it
    CREATE TABLE IF NOT EXISTS UserAssociation
    (
      User_EntityID INT NOT NULL,
      Tag VARCHAR(40) NOT NULL,
      EntityID INT,
      Value FLOAT,
      Info VARCHAR(80),
      PRIMARY KEY (User_EntityID, Tag),
      FOREIGN KEY (User_EntityID) REFERENCES User(EntityID),
      FOREIGN KEY (EntityID) REFERENCES Entity(EntityID)
    );

    -- Permission table -- enumeration of access permissions that users can have on items/accounts
    CREATE TABLE IF NOT EXISTS Permission
    (
      PermissionID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      PRIMARY KEY (PermissionID)
    );

    -- CREATE TABLE IF NOT EXISTS Location/Map
    -- (
    -- );

    -- ItemPropertyType table -- enumeration of the way in which a given property
    --                           combines when two items of the same type share a property
    CREATE TABLE IF NOT EXISTS ItemPropertyType
    (
      ItemPropertyTypeID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      PRIMARY KEY (ItemPropertyTypeID)
    );

    -- BankAccount table -- vital information for bank accounts in the game
    CREATE TABLE IF NOT EXISTS BankAccount
    (
      EntityID INT NOT NULL,
      Credits FLOAT NOT NULL,
      PRIMARY KEY (EntityID),
      FOREIGN KEY (EntityID) REFERENCES Entity(EntityID)
    );

    -- Entity_User_Permissions table -- correlation showing which users have which
    --                                  permissions on a given (non-user) entity
    CREATE TABLE IF NOT EXISTS Entity_User_Permissions
    (
      EntityID INT NOT NULL,
      User_EntityID INT NOT NULL,
      PermissionID INT NOT NULL,
      StatusID INT NOT NULL,
      Created DATETIME,
      Deleted DATETIME,
      FOREIGN KEY (EntityID) REFERENCES Entity(EntityID),
      FOREIGN KEY (User_EntityID) REFERENCES User(EntityID),
      FOREIGN KEY (PermissionID) REFERENCES Permission(PermissionID),
      FOREIGN KEY (StatusID) REFERENCES Status(StatusID)
    );

    -- AccountTransferHistory table -- Records details about movement of money from one
    --                                 bank account to another
    CREATE TABLE AccountTransferHistory
    (
      AccountTransferHistoryID INT NOT NULL AUTO_INCREMENT,
      SourceBankAccountID INT,
      SourceBalancePrior FLOAT,
      DestBankAccountID INT NOT NULL,
      DestBalancePrior FLOAT NOT NULL,
      TransferAmount FLOAT NOT NULL,
      Timestamp DATETIME NOT NULL,
      PRIMARY KEY (AccountTransferHistoryID),
      FOREIGN KEY (SourceBankAccountID) REFERENCES BankAccount(EntityID),
      FOREIGN KEY (DestBankAccountID) REFERENCES BankAccount(EntityID)
    );

    -- ItemProperty table -- enumeration of properties that some game
    --                       items can have but that most don't have
    CREATE TABLE IF NOT EXISTS ItemProperty
    (
      ItemPropertyID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      ItemPropertyTypeID INT NOT NULL,
      PRIMARY KEY (ItemPropertyID),
      FOREIGN KEY (ItemPropertyTypeID) REFERENCES ItemPropertyType(ItemPropertyTypeID)
    );

    -- ItemCategory table -- enumeration of major classifications for item typess
    CREATE TABLE IF NOT EXISTS ItemCategory
    (
      ItemCategoryID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      ItemPropertyID1 INT NOT NULL,
      ItemPropertyID2 INT NOT NULL,
      ItemPropertyID3 INT NOT NULL,
      PRIMARY KEY (ItemCategoryID),
      FOREIGN KEY (ItemPropertyID1) REFERENCES ItemProperty(ItemPropertyID),
      FOREIGN KEY (ItemPropertyID2) REFERENCES ItemProperty(ItemPropertyID),
      FOREIGN KEY (ItemPropertyID3) REFERENCES ItemProperty(ItemPropertyID)
    );

    -- ItemType table -- vital information on individual item types
    CREATE TABLE ItemType
    (
      ItemTypeID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      ItemCategoryID INT NOT NULL,
      Size FLOAT NOT NULL,
      Weight FLOAT NOT NULL,
      Price FLOAT NOT NULL,
      PropertyVal1 FLOAT NOT NULL,
      PropertyVal2 FLOAT NOT NULL,
      PropertyVal3 FLOAT NOT NULL,
      PRIMARY KEY (ItemTypeID),
      FOREIGN KEY (ItemCategoryID) REFERENCES ItemCategory(ItemCategoryID)
    );

    -- Item table -- vital information on individual items
    CREATE TABLE Item
    (
      EntityID INT NOT NULL,
      ItemTypeID INT NOT NULL,
      UsageMonitor FLOAT NOT NULL,
      Name VARCHAR(40), -- if not null, the special/original name of a given item
      Value FLOAT, -- value in credits, superseding the generic value for its type
      PRIMARY KEY (EntityID),
      FOREIGN KEY (ItemTypeID) REFERENCES ItemType(ItemTypeID),
      FOREIGN KEY (EntityID) REFERENCES Entity(EntityID)
    );

    -- Item container -- additional information for containers beyond what is in the item table
    CREATE TABLE IF NOT EXISTS ItemContainer
    (
      EntityID INT NOT NULL, -- must have matching ItemID in Item table to be valid
      Name VARCHAR(40) NOT NULL,
      PRIMARY KEY (EntityID),
      FOREIGN KEY (EntityID) REFERENCES Item(EntityID)
    );

    -- ItemContainer_Item -- correlation indicating which items are in which containers,
    --                       with an indication of how the item is situated in the container
    CREATE TABLE IF NOT EXISTS ItemContainer_Item
    (
      Container_EntityID INT NOT NULL,
      Item_EntityID INT NOT NULL,
      StartSlot INT NOT NULL,   -- first slot in the container where the item appears
      EndSlot INT NOT NULL,     -- last slot in the container where the item appears
      Modifier INT NOT NULL,    -- such as orientation of item in the slots
      PRIMARY KEY (Container_EntityID, Item_EntityID),
      FOREIGN KEY (Container_EntityID) REFERENCES ItemContainer(EntityID),
      FOREIGN KEY (Item_EntityID) REFERENCES Item(EntityID)
    );

    -- ResourceType table -- vital information about resource types
    CREATE TABLE IF NOT EXISTS ResourceType
    (
      ResourceTypeID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      Density FLOAT,             -- Weight per unit size
      PRIMARY KEY (ResourceTypeID)
    );

    -- ImprovementType table -- vital information about types of terrain improvements
    CREATE TABLE IF NOT EXISTS ImprovementType
    (
      ImprovementTypeID INT NOT NULL,
      ResourceTypeID1 INT NOT NULL,
      ResourceTypeID2 INT NOT NULL,
      ResourceTypeID3 INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      Resource1Amount FLOAT,     -- How much of Resource 1 is needed to build?
      Resource2Amount FLOAT,     -- How much of Resource 2 is needed to build?
      Resource3Amount FLOAT,     -- How much of Resource 3 is needed to build?
      Difficulty FLOAT,          -- roughness; NULL = units cannot enter
      MaxSize FLOAT,             -- NULL = no limit; forests might allow trucks/soldiers, fortifications only soldiers, walls nothing
      Strength FLOAT,            -- blocks bullets if non-null; becomes debris at/below zero
      Hardness FLOAT,            -- to drilling/bombardment. Blocks bullets if not null; divide damage by this amount
      Damage FLOAT,              -- inflicted per second on unit present; repairs if negative
      PRIMARY KEY (ImprovementTypeID),
      FOREIGN KEY (ResourceTypeID1) REFERENCES ResourceType(ResourceTypeID),
      FOREIGN KEY (ResourceTypeID2) REFERENCES ResourceType(ResourceTypeID),
      FOREIGN KEY (ResourceTypeID3) REFERENCES ResourceType(ResourceTypeID)
    );

    -- TerrainType table -- vital information about terrain types
    CREATE TABLE IF NOT EXISTS TerrainType
    (
      TerrainTypeID INT NOT NULL,
      ResourceTypeID INT NOT NULL,
      Name VARCHAR(40) NOT NULL,
      Solid BOOLEAN NOT NULL,    -- Land, basically
      Liquid BOOLEAN NOT NULL,   -- Water/magma/etc... coast is both water & land
      Difficulty FLOAT,          -- roughness; NULL = units cannot enter
      MaxSize FLOAT,             -- NULL = no limit; forests might allow trucks/soldiers, fortifications only soldiers, walls nothing
      Strength FLOAT,            -- blocks bullets if non-null; becomes debris at/below zero
      Hardness FLOAT,            -- to drilling/bombardment. Blocks bullets if not null; divide damage by this amount
      Damage FLOAT,              -- inflicted per second on unit present; repairs if negative
      PRIMARY KEY (TerrainTypeID),
      FOREIGN KEY (ResourceTypeID) REFERENCES ResourceType(ResourceTypeID)
    );

-- END