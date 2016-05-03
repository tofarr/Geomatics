package org.jg.gfx.source;

import org.jg.util.View;

/**
 *
 * @author tofarrell
 */
public class CompoundRenderableObjectSource implements RenderableObjectSource {

    private final RenderableObjectSource[] sources;

    private CompoundRenderableObjectSource(RenderableObjectSource[] sources) {
        this.sources = sources;
    }
    
    public static RenderableObjectSource valueOf(RenderableObjectSource... sources) throws NullPointerException{
        switch(sources.length){
            case 0:
                return null;
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

}
