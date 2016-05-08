package org.geomatics.gfx.source;

import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public class CompoundRenderableObjectSource<E> implements RenderableObjectSource<E> {

    private final RenderableObjectSource[] sources;

    private CompoundRenderableObjectSource(RenderableObjectSource[] sources) {
        this.sources = sources;
    }
    
    public static RenderableObjectSource valueOf(RenderableObjectSource... sources) throws NullPointerException{
        switch(sources.length){
            case 0:
                return SimpleRenderableObjectSource.EMPTY;
            case 1:
                return sources[0];
            default:
                return new CompoundRenderableObjectSource(sources.clone());
        }
    }

    public RenderableObjectSource[] getSources() {
        return sources.clone();
    }

    @Override
    public boolean load(View view, RenderableObjectProcessor processor) {
        for(RenderableObjectSource source : sources){
            if(!source.load(view, processor)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean load(ViewPoint viewPoint, RenderableObjectProcessor processor) {
        for(RenderableObjectSource source : sources){
            if(!source.load(viewPoint, processor)){
                return false;
            }
        }
        return true;
    }

    @Override
    public E getAttributes(long renderableId) {
        for(RenderableObjectSource source : sources){
            E attributes = getAttributes(renderableId);
            if(attributes != null){
                return attributes;
            }
        }
        return null;
    }

}
