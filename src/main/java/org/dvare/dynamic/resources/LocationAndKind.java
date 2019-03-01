/*The MIT License (MIT)

Copyright (c) 2019 Muhammad Hammad

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Sogiftware.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/


package org.dvare.dynamic.resources;

import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

public class LocationAndKind {
    private final Kind kind;
    private final Location location;

    public LocationAndKind(Location location, Kind kind) {
        this.location = location;
        this.kind = kind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof LocationAndKind) {
            LocationAndKind other = (LocationAndKind) obj;
            return location.equals(other.location) && kind.equals(other.kind);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return kind.hashCode() * 31 + location.hashCode();
    }

    @Override
    public String toString() {
        return kind.toString() + "@" + location.toString();
    }
}