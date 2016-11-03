package org.openRealmOfStars.starMap;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.openRealmOfStars.gui.infopanel.MapInfoPanel;
import org.openRealmOfStars.gui.mapPanel.MapPanel;
import org.openRealmOfStars.player.PlayerInfo;
import org.openRealmOfStars.player.fleet.Fleet;
import org.openRealmOfStars.starMap.planet.Planet;
import org.openRealmOfStars.utilities.PixelsToMapCoordinate;

/**
 *
 * Open Realm of Stars game project
 * Copyright (C) 2016  Tuomo Untinen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 *
 * Mouse Listener for Star Map
 *
 */

public class StarMapMouseListener extends MouseAdapter
    implements MouseMotionListener {

  /**
   * Star map which to use
   */
  private StarMap starMap;

  /**
   * Map panel which to use
   */
  private MapPanel mapPanel;

  /**
   * Info panel next to map panel
   */
  private MapInfoPanel mapInfoPanel;

  /**
   * Is route being planned
   */
  private boolean routePlanning;

  /**
   * Constructor for StarMap Mouse Listener
   * @param starMap StarMap which to use.
   * @param panel Map Panel where Star Map is being drawn.
   */
  public StarMapMouseListener(final StarMap starMap, final MapPanel panel,
      final MapInfoPanel mapInfoPanel) {
    this.starMap = starMap;
    this.mapPanel = panel;
    this.mapInfoPanel = mapInfoPanel;
  }

  /**
   * Is cursor on border
   */
  private boolean onBorder;

  /**
   * Last know coordinates
   */
  private PixelsToMapCoordinate coord;

  /**
   * Last clicked planet
   * This is Planet which was last clicked, null if empty space or
   * other was clicked
   */
  private Planet lastClickedPlanet;

  /**
   * Last clicked fleet
   * This is fleet which was last clicked, null if empty space or
   * other was clicked
   */
  private Fleet lastClickedFleet;

  /**
   * Something has been double clicked
   */
  private boolean doubleClicked;

  /**
   * Move clicked
   */
  private boolean moveClicked;

  /**
   * Move point where clicked on map X coordinate
   */
  private int moveX;
  /**
   * Move point where clicked on map Y coordinate
   */
  private int moveY;

  public int getMoveX() {
    return moveX;
  }

  public int getMoveY() {
    return moveY;
  }

  /**
   * Update drawing coordinates if mouse cursor is on map border
   */
  public void updateScrollingIfOnBorder() {
    if (coord != null && !coord.isOutOfPanel() && onBorder) {
      if (coord.getRelativeX() == mapPanel.getViewPointX()) {
        starMap.setDrawPos(starMap.getDrawX() + 1, starMap.getDrawY());
      }
      if (coord.getRelativeX() == -mapPanel.getViewPointX()) {
        starMap.setDrawPos(starMap.getDrawX() - 1, starMap.getDrawY());
      }
      if (coord.getRelativeY() == mapPanel.getViewPointY()) {
        starMap.setDrawPos(starMap.getDrawX(), starMap.getDrawY() + 1);
      }
      if (coord.getRelativeY() == -mapPanel.getViewPointY()) {
        starMap.setDrawPos(starMap.getDrawX(), starMap.getDrawY() - 1);
      }
    }

  }

  @Override
  public void mouseExited(final MouseEvent e) {
    coord = null;
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    coord = new PixelsToMapCoordinate(mapPanel.getLastDrawnX(),
        mapPanel.getLastDrawnY(), e.getX(), e.getY(), mapPanel.getOffsetX(),
        mapPanel.getOffsetY(), mapPanel.getViewPointX(),
        mapPanel.getViewPointY(), false);
    if (!coord.isOutOfPanel()) {
      starMap.setCursorPos(coord.getMapX(), coord.getMapY());
      if (routePlanning && lastClickedFleet != null) {
        Route route = new Route(lastClickedFleet.getX(),
            lastClickedFleet.getY(), coord.getMapX(), coord.getMapY(),
            lastClickedFleet.getFleetFtlSpeed());
        mapPanel.setRoute(route);
      } else {
        routePlanning = false;
        mapPanel.setRoute(null);
      }
      boolean tempBorder = false;
      if (coord.getRelativeX() == mapPanel.getViewPointX()) {
        tempBorder = true;
      }
      if (coord.getRelativeX() == -mapPanel.getViewPointX()) {
        tempBorder = true;
      }
      if (coord.getRelativeY() == mapPanel.getViewPointY()) {
        tempBorder = true;
      }
      if (coord.getRelativeY() == -mapPanel.getViewPointY()) {
        tempBorder = true;
      }
      onBorder = tempBorder;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    setDoubleClicked(false);
    setMoveClicked(false);
    coord = new PixelsToMapCoordinate(mapPanel.getLastDrawnX(),
        mapPanel.getLastDrawnY(), e.getX(), e.getY(), mapPanel.getOffsetX(),
        mapPanel.getOffsetY(), mapPanel.getViewPointX(),
        mapPanel.getViewPointY(), false);
    if (!coord.isOutOfPanel()) {
      if (getLastClickedFleet() != null && mapPanel.getRoute() != null) {
        getLastClickedFleet().setRoute(mapPanel.getRoute());
        mapPanel.setRoute(null);
        routePlanning = false;
      }
      if (starMap.getCurrentPlayerInfo().getSectorVisibility(coord.getMapX(),
          coord.getMapY()) == PlayerInfo.VISIBLE
          || e.getButton() == MouseEvent.BUTTON3) {
        Planet planet = starMap.getPlanetByCoordinate(coord.getMapX(),
            coord.getMapY());
        Fleet fleet = starMap.getFleetByCoordinate(coord.getMapX(),
            coord.getMapY());
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          // Double click Button1
          setDoubleClicked(true);
          if (planet != null) {
            setLastClickedPlanet(planet);
            setLastClickedFleet(null);
            mapInfoPanel.showPlanet(planet);
          } else if (fleet != null) {
            setLastClickedPlanet(null);
            setLastClickedFleet(fleet);
            mapInfoPanel.showFleet(fleet);
          }
        } else if (e.getClickCount() == 1
            && e.getButton() == MouseEvent.BUTTON3) {
          // single click right button
          if (lastClickedFleet != null) {
            moveX = coord.getMapX();
            moveY = coord.getMapY();
            double distance = StarMapUtilities.getDistance(
                lastClickedFleet.getX(), lastClickedFleet.getY(), moveX, moveY);
            if (distance < 2 && distance > 0) {
              moveClicked = true;
            }
          }
        } else if (e.getClickCount() == 1
            && e.getButton() == MouseEvent.BUTTON1) {
          // Single click on left button
          if (lastClickedPlanet == planet && fleet != null) {
            mapInfoPanel.showFleet(fleet);
            setLastClickedFleet(fleet);
            setLastClickedPlanet(null);
          } else if (planet != null) {
            mapInfoPanel.showPlanet(planet);
            setLastClickedPlanet(planet);
            setLastClickedFleet(null);
          } else if (fleet != null) {
            mapInfoPanel.showFleet(fleet);
            setLastClickedFleet(fleet);
          } else {
            mapInfoPanel.showEmpty();
            setLastClickedPlanet(null);
            setLastClickedFleet(null);
          }
        }
      } else {
        mapInfoPanel.showEmpty();
        setLastClickedPlanet(null);
        setLastClickedFleet(null);
      }
    }

  }

  /**
   * @return the lastClickedPlanet
   */
  public Planet getLastClickedPlanet() {
    return lastClickedPlanet;
  }

  /**
   * @param lastClickedPlanet the lastClickedPlanet to set
   */
  public void setLastClickedPlanet(final Planet lastClickedPlanet) {
    this.lastClickedPlanet = lastClickedPlanet;
  }

  public Fleet getLastClickedFleet() {
    return lastClickedFleet;
  }

  public void setLastClickedFleet(final Fleet lastClickedFleet) {
    this.lastClickedFleet = lastClickedFleet;
  }

  public boolean isRoutePlanning() {
    return routePlanning;
  }

  public void setRoutePlanning(final boolean routePlanning) {
    this.routePlanning = routePlanning;
  }

  public boolean isDoubleClicked() {
    return doubleClicked;
  }

  public void setDoubleClicked(final boolean doubleClicked) {
    this.doubleClicked = doubleClicked;
  }

  public boolean isMoveClicked() {
    return moveClicked;
  }

  public void setMoveClicked(final boolean moveClicked) {
    this.moveClicked = moveClicked;
  }

}
