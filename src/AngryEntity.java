import processing.core.PImage;

import java.util.*;


public class AngryEntity extends MovableEntity implements ExecutableEntity{
    private static int pathTimes = 0;

    private static final String SAPLING_KEY = "sapling";


    public AngryEntity(String id, Point position, List<PImage> images, int actionPeriod, int animationPeriod) {
        super(id,position,images,animationPeriod,actionPeriod);
    }

    public boolean moveToAngry(
            WorldModel world,
            LasagnaEntity target,
            EventScheduler scheduler) {
        if (adjacent(this.getPosition(), target.getPosition())) {
            target.subHealth();
            if (target.getHealth() == 0){
                world.removeEntity(target);
            }
            scheduler.unscheduleAllEvents(target);
            return true;
        } else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!this.getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity( this, nextPos);
            }
            return false;
        }
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler){
        List<Class> l = new ArrayList<>();
        l.add(TreeEntity.class);
        LasagnaEntity fairyTarget = VirtualWorld.theDude;
        moveToAngry(world, fairyTarget, scheduler);



//        if (fairyTarget.isPresent() && moveToFairy(world, fairyTarget.get(), scheduler)) {
//            world.removeEntity(VirtualWorld.theDude);
//        }

        scheduler.scheduleEvent(this,
                new ActivityAction(this, world, imageStore),
                getActionPeriod());
    }

    public Point nextPosition(WorldModel world, Point destPos) {
        Point newPos;
        PathingStrategy pathing = new AStarPathingStrategy();
        List<Point> newPointsList = pathing.computePath(this.getPosition(), destPos,
                p ->  world.withinBounds(p) && !(world.getOccupancyCell(p) instanceof ObstacleEntity),
                MovableEntity::neighbors,
                PathingStrategy.DIAGONAL_CARDINAL_NEIGHBORS);

        if(newPointsList.size() != 0)
            newPos = newPointsList.get(0);
        else{
            newPos = getPosition();
        }
        return newPos;
    }

    public void scheduleActions(
            EventScheduler scheduler,
            WorldModel world,
            ImageStore imageStore) {
        scheduler.scheduleEvent(this,
                new ActivityAction(this, world, imageStore),
                getActionPeriod());
        scheduler.scheduleEvent(this,
                new AnimationAction(this,0),
                getAnimationPeriod());
    }
}