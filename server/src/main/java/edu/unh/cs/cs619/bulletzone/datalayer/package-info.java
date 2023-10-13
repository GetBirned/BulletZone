/**
 * Classes that provide object-oriented access to the BulletZone server-side database.
 *
 * The main point of access is BulletZoneData, which provides access to the main types of
 * objects you are likely to want to access: GameItems, GameItemContainers, Users,
 * and BankAccounts
 *
 * This package makes use of mariadb. To use this with gradle, add the following dependency
 * into the build.gradle file for the component that is using this package
 *      implementation 'org.mariadb.jdbc:mariadb-java-client:2.1.2'
 *
 * @since 0.1
 * @author mdp (Matthew Plumlee)
 * @version 0.1
 *
 */

package edu.unh.cs.cs619.bulletzone.datalayer;
